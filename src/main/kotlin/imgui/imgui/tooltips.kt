package imgui.imgui

import imgui.*
import imgui.ImGui.begin
import imgui.ImGui.currentWindowRead
import imgui.ImGui.end
import imgui.ImGui.findWindowByName
import imgui.ImGui.style
import imgui.ImGui.textV
import imgui.Context as g

/** Tooltips    */
interface imgui_tooltips {

    /** Not exposed publicly as BeginTooltip() because bool parameters are evil. Let's see if other needs arise first.  */
    fun beginTooltipEx(overridePreviousTooltip: Boolean) {

        var windowName = "##Tooltip%02d".format(style.locale, g.tooltipOverrideCount)
        if (overridePreviousTooltip)
            findWindowByName(windowName)?.let {
                if (it.active) {
                    // Hide previous tooltips. We can't easily "reset" the content of a window so we create a new one.
                    it.hiddenFrames = 1
                    windowName = "##Tooltip%02d".format(++g.tooltipOverrideCount)
                }
            }
        begin(windowName, null, WindowFlags.Tooltip or WindowFlags.NoTitleBar or WindowFlags.NoMove or
                WindowFlags.NoResize or WindowFlags.NoSavedSettings or WindowFlags.AlwaysAutoResize)
    }

    fun setTooltipV(fmt: String, args: Array<out Any>) {
        beginTooltipEx(true)
        textV(fmt, args)
        endTooltip()
    }

    fun setTooltip(fmt: String, vararg args: Any)  = setTooltipV(fmt, args)

    /** begin/append a tooltip window. to create full-featured tooltip (with any kind of contents). */
    fun beginTooltip() = beginTooltipEx(false)

    fun endTooltip() {
        assert(currentWindowRead!!.flags has WindowFlags.Tooltip)   // Mismatched BeginTooltip()/EndTooltip() calls
        end()
    }
}