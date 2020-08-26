package cz.covid19cz.erouska.net

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.gson.Gson
import cz.covid19cz.erouska.net.model.Data
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CustomConverterFactory(gson: Gson) : Converter.Factory() {

    private val gsonConverterFactory: GsonConverterFactory = GsonConverterFactory.create(gson)

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        val wrappedType = object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> = arrayOf(type)
            override fun getOwnerType(): Type? = null
            override fun getRawType(): Type = Data::class.java
        }
        val gsonConverter: Converter<ResponseBody, *>? = gsonConverterFactory.responseBodyConverter(wrappedType, annotations, retrofit)
        return ResponseBodyConverter(gsonConverter as Converter<ResponseBody, Data<Any>>)
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>,
                                      methodAnnotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return gsonConverterFactory.requestBodyConverter(type!!, parameterAnnotations, methodAnnotations, retrofit)
    }
}

class ResponseBodyConverter<T>(private val converter: Converter<ResponseBody, Data<T>>) :
    Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(responseBody: ResponseBody): T {
        val response = converter.convert(responseBody)

        response?.data?.let {
            return it
        } ?: throw ApiException(Status(-1))
    }
}