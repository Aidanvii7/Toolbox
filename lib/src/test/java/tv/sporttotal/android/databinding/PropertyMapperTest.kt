package tv.sporttotal.android.databinding

import com.nhaarman.mockito_kotlin.whenever
import de.jodamob.kotlin.reflect.on
import de.jodamob.kotlin.reflect.set
import de.jodamob.kotlin.reflect.to
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty

class PropertyMapperTest() {

    companion object {
        const val PROPERTY_1_NAME = "property1"
        const val PROPERTY_2_NAME = "property2"
        const val PROPERTY_3_NAME = "property3"

        init {
            PropertyMapper.initBRClass(TestBR::class.java)
        }
    }

    @AfterEach
    fun after() {
        on(PropertyMapper) set "locked" to false
    }

    @Test
    fun `initBRClass throws IllegalStateException if previously called with locked as true`() {
        PropertyMapper.initBRClass(TestBR::class.java, locked = true)

        val initBRClass = { PropertyMapper.initBRClass(TestBR::class.java) }

        initBRClass shouldThrow IllegalStateException::class
    }

    @Test
    fun `initBRClass does not throw IllegalStateException if not previously called with locked as true`() {
        PropertyMapper.initBRClass(TestBR::class.java, locked = false)
        PropertyMapper.initBRClass(TestBR::class.java)
    }

    @Test
    fun `when property given with PROPERTY_1_NAME then propertyId returned with PROPERTY_1_ID`() {
        val kProperty = PROPERTY_1_NAME.toKPropertyMock()

        val actualPropertyId = PropertyMapper.getBindableResourceId(kProperty)

        actualPropertyId `should be equal to` TestBR.property1
    }

    @Test
    fun `when property given with PROPERTY_2_NAME then propertyId returned with PROPERTY_2_ID`() {
        val kProperty = PROPERTY_2_NAME.toKPropertyMock()

        val actualPropertyId = PropertyMapper.getBindableResourceId(kProperty)

        actualPropertyId `should be equal to` TestBR.property2
    }

    @Test
    fun `when property given with PROPERTY_3_NAME then propertyId returned with PROPERTY_3_ID`() {
        val kProperty = PROPERTY_3_NAME.toKPropertyMock()

        val actualPropertyId = PropertyMapper.getBindableResourceId(kProperty)

        actualPropertyId `should be equal to` TestBR.property3
    }

    @Test
    fun `resourceIds returns ordered list of all constants in given BR class`() {
        val expectedResourceIds = arrayOf(TestBR._all, TestBR.property1, TestBR.property2, TestBR.property3)
        PropertyMapper.resourceIds.toList() `should equal` expectedResourceIds.toList()
    }

    fun String.toKPropertyMock(): KProperty<*> {
        return mock<KProperty<*>>().apply {
            whenever(name).thenReturn(this@toKPropertyMock)
        }
    }
}