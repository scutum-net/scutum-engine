import java.lang.reflect.Type

import com.google.gson._


val gson: Gson = new GsonBuilder().create()


val x = scala.io.Source.fromFile("/Users/dstatsen/Downloads/events.json")
  .getLines()
  .map(i => gson.toJsonTree(i))
  .toList

val y = x(0)
y.