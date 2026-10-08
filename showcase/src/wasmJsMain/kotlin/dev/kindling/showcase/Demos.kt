package dev.kindling.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.kindling.core.components.ui.KInput
import dev.kindling.core.components.ui.KLabel
import dev.kindling.core.components.ui.KSkeleton
import dev.kindling.core.components.ui.KTextarea
import dev.kindling.core.components.ui.avatar.KAvatar
import dev.kindling.core.components.ui.avatar.KAvatarFallback
import dev.kindling.core.components.ui.avatar.KAvatarGroup
import dev.kindling.core.components.ui.avatar.KAvatarGroupCount
import dev.kindling.core.components.ui.avatar.KAvatarSize
import dev.kindling.core.components.ui.badge.KBadge
import dev.kindling.core.components.ui.badge.KBadgeVariant
import dev.kindling.core.components.ui.button.KButton
import dev.kindling.core.components.ui.button.KButtonSize
import dev.kindling.core.components.ui.button.KButtonVariant
import dev.kindling.core.components.ui.card.KCard
import dev.kindling.core.components.ui.card.KCardContent
import dev.kindling.core.components.ui.card.KCardDescription
import dev.kindling.core.components.ui.card.KCardFooter
import dev.kindling.core.components.ui.card.KCardHeader
import dev.kindling.core.components.ui.card.KCardTitle
import dev.kindling.core.components.ui.dialog.Dialog
import dev.kindling.core.components.ui.dialog.DialogContent
import dev.kindling.core.components.ui.dialog.DialogDescription
import dev.kindling.core.components.ui.dialog.DialogFooter
import dev.kindling.core.components.ui.dialog.DialogHeader
import dev.kindling.core.components.ui.dialog.DialogTitle
import dev.kindling.core.components.ui.empty.KEmptyState
import dev.kindling.core.components.ui.pagination.Pagination
import dev.kindling.core.components.ui.pagination.PaginationContent
import dev.kindling.core.components.ui.pagination.PaginationEllipsis
import dev.kindling.core.components.ui.pagination.PaginationItem
import dev.kindling.core.components.ui.pagination.PaginationLink
import dev.kindling.core.components.ui.pagination.PaginationNext
import dev.kindling.core.components.ui.pagination.PaginationPrevious
import dev.kindling.core.components.ui.spinner.KSpinner
import dev.kindling.core.components.ui.spinner.KSpinnerSize

@Composable
fun BadgeDemo() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        KBadge { Text("Default") }
        KBadge(variant = KBadgeVariant.Secondary) { Text("Secondary") }
        KBadge(variant = KBadgeVariant.Destructive) { Text("Destructive") }
        KBadge(variant = KBadgeVariant.Outline) { Text("Outline") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ButtonDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KButtonVariant.entries.forEach { KButton(text = it.name, onClick = {}, variant = it) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            KButton(text = "Small", onClick = {}, size = KButtonSize.Sm)
            KButton(text = "Default", onClick = {})
            KButton(text = "Large", onClick = {}, size = KButtonSize.Lg)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KButton(text = "Please wait", onClick = {}, isLoading = true)
            KButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@Composable
fun CardDemo() {
    var name by remember { mutableStateOf("") }
    KCard(modifier = Modifier.width(360.dp)) {
        KCardHeader {
            KCardTitle("Create project")
            KCardDescription("Deploy your new project in one click.")
        }
        KCardContent {
            KInput(value = name, onValueChange = { name = it }, placeholder = "Project name")
        }
        KCardFooter {
            KButton(text = "Cancel", onClick = {}, variant = KButtonVariant.Outline)
            KButton(text = "Deploy", onClick = {})
        }
    }
}

@Composable
fun AvatarDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            KAvatar(size = KAvatarSize.Sm) { KAvatarFallback(initials = "CB") }
            KAvatar { KAvatarFallback(initials = "CB") }
            KAvatar(size = KAvatarSize.Lg) { KAvatarFallback(initials = "CB") }
        }
        KAvatarGroup {
            listOf("AB", "CD", "EF").forEach { initials ->
                KAvatar { KAvatarFallback(initials = initials) }
            }
            KAvatarGroupCount(count = 4)
        }
    }
}

@Composable
fun InputDemo() {
    var text by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KInput(value = text, onValueChange = { text = it }, placeholder = "Email")
        KInput(value = password, onValueChange = { password = it }, placeholder = "Password", isPassword = true)
        KInput(value = "not-an-email", onValueChange = {}, isError = true)
        KInput(value = "Disabled", onValueChange = {}, enabled = false)
    }
}

@Composable
fun LabelDemo() {
    var email by remember { mutableStateOf("") }
    Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KLabel(text = "Email")
        KInput(value = email, onValueChange = { email = it }, placeholder = "m@example.com")
        KLabel(text = "Disabled label", disabled = true)
    }
}

@Composable
fun TextareaDemo() {
    var message by remember { mutableStateOf("") }
    Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KLabel(text = "Your message")
        KTextarea(value = message, onValueChange = { message = it }, placeholder = "Type your message here.", minLines = 4)
    }
}

@Composable
fun SkeletonDemo() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        KSkeleton(Modifier.size(48.dp).clip(CircleShape))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KSkeleton(Modifier.height(16.dp).width(250.dp))
            KSkeleton(Modifier.height(16.dp).width(200.dp))
        }
    }
}

@Composable
fun SpinnerDemo() {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        KSpinner(size = KSpinnerSize.Sm)
        KSpinner()
        KSpinner(size = KSpinnerSize.Lg)
        KSpinner(size = KSpinnerSize.Xl)
    }
}

@Composable
fun DialogDemo() {
    var open by remember { mutableStateOf(false) }
    KButton(text = "Edit profile", onClick = { open = true }, variant = KButtonVariant.Outline)
    Dialog(open = open, onOpenChange = { open = it }) {
        DialogContent(open = open, onDismiss = { open = false }) {
            DialogHeader {
                DialogTitle("Edit profile")
                DialogDescription("Make changes to your profile here. Click save when you're done.")
            }
            DialogFooter(showCloseButton = true, onDismiss = { open = false }) {
                KButton(text = "Save changes", onClick = { open = false })
            }
        }
    }
}

@Composable
fun PaginationDemo() {
    var page by remember { mutableStateOf(2) }
    val last = 3
    Pagination {
        PaginationContent {
            PaginationItem { PaginationPrevious(onClick = { page = maxOf(1, page - 1) }, enabled = page > 1) }
            (1..last).forEach { p ->
                PaginationItem { PaginationLink(page = p, isActive = p == page, onClick = { page = p }) }
            }
            PaginationItem { PaginationEllipsis() }
            PaginationItem { PaginationNext(onClick = { page = minOf(last, page + 1) }, enabled = page < last) }
        }
    }
}

@Composable
fun EmptyDemo() {
    KEmptyState(
        icon = Icons.Outlined.Inbox,
        title = "No messages",
        description = "You're all caught up. New messages will appear here.",
        actionLabel = "Refresh",
        onAction = {},
    )
}
