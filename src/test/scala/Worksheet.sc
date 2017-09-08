import com.google.gson.GsonBuilder
import scutum.core.contracts.ScannedData

val x = new ScannedData(1,1,1,"postman")

val gson = new GsonBuilder().create()

val z = gson.toJson(x)