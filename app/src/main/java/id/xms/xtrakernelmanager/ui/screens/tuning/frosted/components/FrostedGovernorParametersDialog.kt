package id.xms.xtrakernelmanager.ui.screens.tuning.frosted.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import id.xms.xtrakernelmanager.ui.components.GlassmorphicCard
import id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialog
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import kotlinx.coroutines.launch

@Composable
fun FrostedGovernorParametersDialog(
    clusterIndex: Int,
    clusterName: String,
    governor: String,
    viewModel: TuningViewModel,
    onDismiss: () -> Unit
) {
    var parameters by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var editingParameter by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
    // Load parameters when dialog opens
    LaunchedEffect(clusterIndex, governor) {
        isLoading = true
        parameters = viewModel.getGovernorParameters(clusterIndex)
        isLoading = false
    }
    
    FrostedDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Governor Parameters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$clusterName • $governor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        content = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (parameters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No parameters available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "This governor has no tunable parameters",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(parameters.toList().sortedBy { it.first }) { (paramName, paramValue) ->
                        FrostedParameterItem(
                            parameterName = paramName,
                            parameterValue = paramValue,
                            isEditing = editingParameter == paramName,
                            editValue = editValue,
                            onEditValueChange = { editValue = it },
                            onEditClick = {
                                editingParameter = paramName
                                editValue = paramValue
                            },
                            onSaveClick = {
                                scope.launch {
                                    viewModel.setGovernorParameter(clusterIndex, paramName, editValue)
                                    // Reload parameters to show updated value
                                    parameters = viewModel.getGovernorParameters(clusterIndex)
                                    editingParameter = null
                                }
                            },
                            onCancelClick = {
                                editingParameter = null
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            id.xms.xtrakernelmanager.ui.components.frosted.FrostedDialogButton(
                text = "Close",
                onClick = onDismiss,
                isPrimary = true
            )
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Composable
private fun FrostedParameterItem(
    parameterName: String,
    parameterValue: String,
    isEditing: Boolean,
    editValue: String,
    onEditValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Parameter name
            Text(
                text = parameterName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isEditing) {
                // Edit mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = onEditValueChange,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )
                    
                    // Save button
                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Rounded.Save,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                    
                    // Cancel button
                    IconButton(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Cancel",
                            tint = Color.White
                        )
                    }
                }
            } else {
                // View mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEditClick)
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = parameterValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
