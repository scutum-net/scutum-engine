import java.lang.reflect.Type

import com.google.gson._


case class Dog(name: String, age: Int)

val gson: Gson = new GsonBuilder().create()

val d = Dog("Rex", 3)
val s = gson.toJson(d)
val z = gson.fromJson(s, classOf[Dog])
