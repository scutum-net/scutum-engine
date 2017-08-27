import com.google.gson.{Gson, GsonBuilder}

case class A(id: Int)

val serializer: Gson = new GsonBuilder().create()

val a = A(1)
val s = serializer.toJson(a)
serializer.fromJson(s, classOf[A])


