package app.crimera.patches.twitter.misc.fold

import app.crimera.patches.twitter.misc.blockRedirectToXLite.blockRedirectingToXLitePatch
import app.crimera.patches.twitter.misc.extension.twitterInitHook
import app.crimera.patches.twitter.misc.settings.settingsPatch
import app.crimera.patches.twitter.utils.Constants.COMPATIBILITY_X
import app.crimera.patches.twitter.utils.enableSettings
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.resources.ResourceType
import app.morphe.patches.all.misc.resources.resourceLiteral
import app.morphe.patches.all.misc.resources.resourceMappingPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val FOLD = "Lapp/morphe/extension/twitter/patches/fold/FoldLayout;"

private object ChatMotionFingerprint : Fingerprint(
    definingClass = "Lcom/x/compose/decompose/u0;",
    name = "a",
    returnType = "V",
    filters = listOf(
        fieldAccess(opcode = Opcode.SGET_OBJECT, definingClass = "Lcom/x/compose/decompose/a;", name = "a"),
        opcode(Opcode.AND_INT, location = MatchAfterImmediately()),
    ),
)

private object TabletListInsetFingerprint : Fingerprint(
    filters = listOf(
        resourceLiteral(ResourceType.DIMEN, "tablet_inset_horizontal"),
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Landroid/content/res/Resources;",
            name = "getDimension",
            parameters = listOf("I"),
            returnType = "F",
            location = MatchAfterImmediately(),
        ),
    ),
)

@Suppress("unused")
val foldLandscapePatch = bytecodePatch(
    name = "Fold landscape 4:3 layout",
    description = "Adds side navigation, post/replies columns, native inbox/conversation panels and reduced XChat motion " +
        "on supported devices in large landscape 4:3 windows. Portrait stays unchanged.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_X)
    dependsOn(settingsPatch, blockRedirectingToXLitePatch, resourceMappingPatch, foldResourcePatch)

    execute {
        twitterInitHook.fingerprint.method.apply {
            val returnIndex = instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            check(returnIndex >= 0) { "Twitter Application.onCreate has no return" }
            addInstruction(returnIndex, "invoke-static/range {p0 .. p0}, $FOLD->initialize(Landroid/app/Application;)V")
        }
        TabletListInsetFingerprint.let {
            val index = it.instructionMatches.last().index
            val call = it.method.getInstruction<FiveRegisterInstruction>(index)
            it.method.replaceInstruction(index,
                "invoke-static {v${call.registerC}, v${call.registerD}}, $FOLD->tabletInset(Landroid/content/res/Resources;I)F")
        }
        ChatMotionFingerprint.let {
            val index = it.instructionMatches.last().index
            val register = it.method.getInstruction<OneRegisterInstruction>(it.instructionMatches.first().index).registerA
            it.method.addInstructions(index + 1, """
                invoke-static {v$register}, Lapp/morphe/extension/twitter/patches/fold/FoldChat;->animator(Ljava/lang/Object;)Ljava/lang/Object;
                move-result-object v$register
                check-cast v$register, Lcom/arkivanov/decompose/extensions/compose/experimental/stack/animation/x0;
            """)
        }
        enableSettings("foldLayout")
    }
}
