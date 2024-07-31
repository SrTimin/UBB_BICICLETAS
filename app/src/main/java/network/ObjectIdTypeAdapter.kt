import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.mongodb.kbson.ObjectId

class ObjectIdTypeAdapter : TypeAdapter<ObjectId>() {
    override fun write(out: JsonWriter, value: ObjectId) {
        out.beginObject()
        out.name("\$oid").value(value.toHexString())
        out.endObject()
    }

    override fun read(`in`: JsonReader): ObjectId {
        return when (`in`.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                `in`.beginObject()
                var objectId: ObjectId? = null
                while (`in`.hasNext()) {
                    if (`in`.nextName() == "\$oid") {
                        objectId = ObjectId(`in`.nextString())
                    }
                }
                `in`.endObject()
                objectId ?: throw IllegalStateException("Expected \$oid field")
            }
            JsonToken.STRING -> ObjectId(`in`.nextString())
            else -> throw IllegalStateException("Expected BEGIN_OBJECT or STRING but was ${`in`.peek()}")
        }
    }
}
