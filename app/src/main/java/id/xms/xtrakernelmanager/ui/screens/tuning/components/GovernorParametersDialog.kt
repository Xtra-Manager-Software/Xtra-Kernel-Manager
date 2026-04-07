package id.xms.xtrakernelmanager.ui.screens.tuning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.xms.xtrakernelmanager.ui.screens.tuning.TuningViewModel
import kotlinx.coroutines.launch

@Composable
fun GovernorParametersDialog(
    clusterIndex: Int,
    clusterName: String,
    governor: String,
    viewModel: TuningViewModel,
    onDismiss: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onSurfaceColor: Color = MaterialTheme.colorScheme.onSurface,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    surfaceVariantColor: Color = MaterialTheme.colorScheme.surfaceVariant
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
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = containerColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Governor Parameters",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$clusterName • $governor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = onSurfaceColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else if (parameters.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                tint = onSurfaceColor.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "No parameters available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = onSurfaceColor.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "This governor has no tunable parameters",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(parameters.toList().sortedBy { it.first }) { (paramName, paramValue) ->
                            ParameterItem(
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
                                },
                                surfaceVariantColor = surfaceVariantColor,
                                onSurfaceColor = onSurfaceColor,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParameterItem(
    parameterName: String,
    parameterValue: String,
    isEditing: Boolean,
    editValue: String,
    onEditValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    surfaceVariantColor: Color,
    onSurfaceColor: Color,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = surfaceVariantColor
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
                color = onSurfaceColor
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
                            fontFamily = FontFamily.Monospace
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.3f),
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor
                        ),
                        singleLine = true
                    )
                    
                    // Save button
                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(primaryColor, RoundedCornerShape(8.dp))
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
                            .border(1.dp, onSurfaceColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Cancel",
                            tint = onSurfaceColor
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
                            onSurfaceColor.copy(alpha = 0.05f),
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
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
