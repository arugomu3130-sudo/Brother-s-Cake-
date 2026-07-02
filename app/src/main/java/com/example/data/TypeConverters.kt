package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class BakeryConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val orderItemListType = Types.newParameterizedType(List::class.java, OrderItem::class.java)
    private val adapter = moshi.adapter<List<OrderItem>>(orderItemListType)

    @TypeConverter
    fun fromOrderItemList(value: List<OrderItem>?): String? {
        return value?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toOrderItemList(value: String?): List<OrderItem>? {
        return value?.let { adapter.fromJson(it) }
    }
}
