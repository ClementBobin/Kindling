package dev.kindling.showcase

import androidx.compose.runtime.Composable

/**
 * One live demo. [id] must equal the docs page slug (e.g. `badge`, `input-otp`): the docs generator
 * (website/scripts/docgen.mjs) scans this file for `demo("<id>"` and only embeds a preview on pages
 * that have a matching demo.
 *
 * To add a demo: write a composable in `demos/`, then register it below.
 */
class Demo(val id: String, val title: String, val content: @Composable () -> Unit)

private fun demo(id: String, title: String, content: @Composable () -> Unit) = Demo(id, title, content)

val demos: List<Demo> = listOf(
    demo("avatar", "Avatar") { AvatarDemo() },
    demo("badge", "Badge") { BadgeDemo() },
    demo("button", "Button") { ButtonDemo() },
    demo("card", "Card") { CardDemo() },
    demo("dialog", "Dialog") { DialogDemo() },
    demo("empty", "Empty") { EmptyDemo() },
    demo("input", "Input") { InputDemo() },
    demo("label", "Label") { LabelDemo() },
    demo("pagination", "Pagination") { PaginationDemo() },
    demo("skeleton", "Skeleton") { SkeletonDemo() },
    demo("spinner", "Spinner") { SpinnerDemo() },
    demo("textarea", "Textarea") { TextareaDemo() },
)
