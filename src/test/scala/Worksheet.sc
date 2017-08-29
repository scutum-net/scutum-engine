import java.lang.reflect.Type

import com.google.gson._


case class B(name: String, age: Int)
case class A(id: Int, b: B)

val serializer: Gson = new GsonBuilder().create()

val a = A(1, B("sami", 2))
val s = serializer.toJson(a)

class Ser extends JsonDeserializer[B] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): B = {
    val j = json.getAsJsonObject.get("b")
    serializer.fromJson(j, classOf[B])
  }
}

val serializer2: Gson = new GsonBuilder().registerTypeAdapter(A.getClass, new Ser).create()
val data = serializer2.fromJson(s, B.getClass)
data