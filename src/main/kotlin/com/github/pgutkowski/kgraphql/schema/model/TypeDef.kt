package com.github.pgutkowski.kgraphql.schema.model

import com.github.pgutkowski.kgraphql.schema.scalar.ScalarCoercion
import com.github.pgutkowski.kgraphql.schema.structure2.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface TypeDef {

    val name : String

    val description : String?

    abstract class BaseKQLType(name : String, override val description: String?) : TypeDef, Definition(name)

    interface Kotlin<T : Any> : TypeDef {
        val kClass : KClass<T>
    }

    class Object<T : Any> (
            name : String,
            override val kClass: KClass<T>,
            val kotlinProperties: Map<KProperty1<T, *>, PropertyDef.Kotlin<T, *>>,
            val extensionProperties : List<PropertyDef.Function<*>>,
            val unionProperties : List<PropertyDef.Union>,
            val transformations : Map<KProperty1<T, *>, Transformation<T, *>>,
            description : String?
    ) : BaseKQLType(name, description), Kotlin<T> {

        constructor (
                name : String, kClass: KClass<T>
        ) : this(name, kClass, emptyMap(), emptyList(), emptyList(), emptyMap(), null)

        fun isIgnored(property: KProperty1<*, *>): Boolean = kotlinProperties[property]?.isIgnored ?: false
    }

    class Input<T : Any>(
            name : String,
            override val kClass: KClass<T>,
            description: String? = null
    ) : BaseKQLType(name, description), Kotlin<T>

    class Scalar<T : Any> (
            name : String,
            override val kClass: KClass<T>,
            val coercion: ScalarCoercion<T, *>,
            description : String?
    ) : BaseKQLType(name, description), Kotlin<T> {
        fun toScalarType() : Type.Scalar<T> = Type.Scalar(this)
    }

    //To avoid circular dependencies etc. union type members are resolved in runtime
    class Union (
            name : String,
            val members: Set<KClass<*>>,
            description : String?
    ) : BaseKQLType(name, description)

    class Enumeration<T : Enum<T>> (
            name: String,
            override val kClass: KClass<T>,
            val values: List<EnumValueDef<T>>,
            description : String? = null
    ) : BaseKQLType(name, description), Kotlin<T> {
        fun toEnumType() : Type.Enum<T> = Type.Enum(this)
    }
}