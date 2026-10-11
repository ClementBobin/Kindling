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
    demo("animated", "Animated") { AnimatedDemo() },
    demo("aspect-ratio", "Aspect Ratio") { AspectRatioDemo() },
    demo("avatar", "Avatar") { AvatarDemo() },
    demo("badge", "Badge") { BadgeDemo() },
    demo("button", "Button") { ButtonDemo() },
    demo("card", "Card") { CardDemo() },
    demo("carousel", "Carousel") { CarouselDemo() },
    demo("charts", "Charts") { ChartsDemo() },
    demo("combobox", "Combobox") { ComboboxDemo() },
    demo("consent", "Consent") { ConsentDemo() },
    demo("dashboard", "Dashboard") { DashboardDemo() },
    demo("datatable", "Data Table") { DataTableDemo() },
    demo("dialog", "Dialog") { DialogDemo() },
    demo("direction", "Direction") { DirectionDemo() },
    demo("empty", "Empty") { EmptyDemo() },
    demo("input", "Input") { InputDemo() },
    demo("input-group", "Input Group") { InputGroupDemo() },
    demo("input-otp", "Input OTP") { InputOTPDemo() },
    demo("label", "Label") { LabelDemo() },
    demo("layout", "Layout") { LayoutDemo() },
    demo("logos-carousel", "Logos Carousel") { LogosCarouselDemo() },
    demo("mask-input", "Mask Input") { MaskInputDemo() },
    demo("masonry", "Masonry") { MasonryDemo() },
    demo("pagination", "Pagination") { PaginationDemo() },
    demo("picker", "Picker") { PickerDemo() },
    demo("popover", "Popover") { PopoverDemo() },
    demo("questionnaire", "Questionnaire") { QuestionnaireDemo() },
    demo("skeleton", "Skeleton") { SkeletonDemo() },
    demo("spinner", "Spinner") { SpinnerDemo() },
    demo("stepper", "Stepper") { StepperDemo() },
    demo("textarea", "Textarea") { TextareaDemo() },
    demo("toast", "Toast") { ToastDemo() },
    demo("upload", "Upload") { UploadDemo() },
)
