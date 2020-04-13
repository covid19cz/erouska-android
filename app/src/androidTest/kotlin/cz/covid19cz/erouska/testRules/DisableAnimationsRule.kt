package cz.covid19cz.erouska.testRules

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Test rule for disabling animations before test start.
 * Animations are re-enabled after test finish.
 *
 * @author Michal Kubele (michal.kubele@gmail.com)
 */
class DisableAnimationsRule : TestRule {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    companion object {
        private const val DISABLED = 0
        private const val ENABLED = 1
        private const val TRANSITION_ANIMATION_SCALE = "settings put global transition_animation_scale %d"
        private const val WINDOW_ANIMATION_SCALE = "settings put global window_animation_scale %d"
        private const val ANIMATOR_DURATION_SCALE = "settings put global animator_duration_scale %d"
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                disableAnimations()
                try {
                    base.evaluate()
                } finally {
                    enableAnimations()
                }
            }
        }
    }

    internal fun enableAnimations() {
        device.run {
            executeCommand(TRANSITION_ANIMATION_SCALE, ENABLED)
            executeCommand(WINDOW_ANIMATION_SCALE, ENABLED)
            executeCommand(ANIMATOR_DURATION_SCALE, ENABLED)
        }
    }

    internal fun disableAnimations() {
        device.run {
            executeCommand(TRANSITION_ANIMATION_SCALE, DISABLED)
            executeCommand(WINDOW_ANIMATION_SCALE, DISABLED)
            executeCommand(ANIMATOR_DURATION_SCALE, DISABLED)
        }
    }
}

/**
 * Executes provided shell [command] with arguments [args] on the device.
 *
 * @param command command to run
 * @param args arguments for command
 */
fun UiDevice.executeCommand(command: String, vararg args: Any) {
    this.executeShellCommand(command.format(*args))
}