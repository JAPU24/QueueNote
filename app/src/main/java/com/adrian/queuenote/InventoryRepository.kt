package com.adrian.queuenote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class ArticuloWrapper(
    @SerializedName("articulos") val items: List<Articulo>?
)

data class Articulo(
    @SerializedName("id_articulo") val id: Int?,
    @SerializedName("articulo") val nombre: String?,
    @SerializedName("existencia") val unidades: Double?,
    @SerializedName("precio") val precio: Double?,
    @SerializedName("costo") val costo: Double?
) {
    val unidadesInt: Int get() = unidades?.toInt() ?: 0
    val precioDouble: Double get() = precio ?: 0.0
    val costoDouble: Double get() = costo ?: 0.0
    val beneficio: Double get() = precioDouble - costoDouble
    val imagenUrl: String? = null // La API que pasaste no trae imagen directa, se queda en null
}

interface InventoryApiService {
    @GET("borrar.php?t=Articulo_Lista_Select")
    suspend fun buscarArticulos(@Query("consulta") consulta: String): ArticuloWrapper
}

class InventoryRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://softecard.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(InventoryApiService::class.java)

    suspend fun getArticulos(query: String): Result<List<Articulo>> {
        return try {
            val response = apiService.buscarArticulos(query)
            Result.success(response.items ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
