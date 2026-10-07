package app.crimera.patches.twitter.misc.fold

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

internal val foldResourcePatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { manifest ->
            val app = manifest.getElementsByTagName("application").item(0)
            fun property(name: String, value: String) {
                val existing = (0 until app.childNodes.length).map { app.childNodes.item(it) }
                    .filterIsInstance<Element>()
                    .firstOrNull { it.tagName == "property" && it.getAttribute("android:name") == name }
                val node = existing ?: manifest.createElement("property").also { app.appendChild(it) }
                node.setAttribute("android:name", name)
                node.setAttribute("android:value", value)
            }
            property("android.window.PROPERTY_ACTIVITY_EMBEDDING_SPLITS_ENABLED", "true")
            property("android.window.PROPERTY_ACTIVITY_EMBEDDING_ALLOW_SYSTEM_OVERRIDE", "false")
        }
    }
}
