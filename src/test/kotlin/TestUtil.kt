import org.jaqpot.client.JaqpotApiClient
import org.junit.platform.commons.util.ReflectionUtils
import java.lang.reflect.Field

class TestUtil {
    companion object {
        fun mockPrivateProperty(instance: Any, propertyName: String, mockedValue: Any) {
            val field: Field = ReflectionUtils
                .findFields(
                    JaqpotApiClient::class.java, { f: Field -> f.name == propertyName },
                    ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
                )[0]

            field.setAccessible(true)
            field.set(instance, mockedValue)
        }
    }

}
