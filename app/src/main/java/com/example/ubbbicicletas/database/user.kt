import io.realm.kotlin.ext.realmListOf;
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant;
import io.realm.kotlin.types.RealmList;
import io.realm.kotlin.types.RealmObject;
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId;

class user : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var carrera: String = ""

    var contacto: RealmList<user_contacto> = realmListOf()

    class user_contacto : EmbeddedRealmObject {
        var correo: String = ""
        var correoPersonal: String? = null
        var numero: Double? = null
    }

    var ingreso: RealmInstant = RealmInstant.now()

    var nombre: String = ""
}
