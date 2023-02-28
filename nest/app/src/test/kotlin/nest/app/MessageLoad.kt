package nest.app

import com.github.jsonldjava.utils.JsonUtils
import java.io.FileInputStream
import org.junit.jupiter.api.Test

class MessageBasics {
    val prefix = "../../examples/"
    val files = listOf("activity_pub/src/routes.json", "activity_pub/src/functions.json")

    fun forEachFile(theFn: (toLoad: String) -> Any?) {
        for (file in files) {

            val toLoad = "${prefix}${file}"

            theFn(toLoad)
        }
    }

    @Test
    fun testLoadMsg() {
        forEachFile({ toLoad: String ->
            val inputStream = FileInputStream(toLoad)
            val jsonObject: Object? = JsonUtils.fromInputStream(inputStream) as Object?
           
            assert(jsonObject != null)
        })
    }
}
