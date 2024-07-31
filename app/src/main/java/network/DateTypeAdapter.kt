import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateTypeAdapter : TypeAdapter<Date>() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun write(out: JsonWriter, value: Date) {
        out.value(dateFormat.format(value))
    }

    override fun read(`in`: JsonReader): Date {
        return dateFormat.parse(`in`.nextString()) ?: throw IllegalStateException("Invalid date format")
    }
}
