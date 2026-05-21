package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun EditorScreen(
    path: String?,
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(path) {
        if (uiState.currentPath != path) {
            viewModel.loadFile(path)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(EditorBackground)) {
        // VS Code style custom Tab / Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(SophisticatedSurface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SophisticatedTextMuted, modifier = Modifier.size(20.dp))
            }
            
            // "Tab"
            if (uiState.fileName.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(EditorBackground) // Active tab color
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.fileName,
                        color = SophisticatedPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (uiState.hasUnsavedChanges) {
                        Box(modifier = Modifier.padding(start = 8.dp).size(8.dp).background(SophisticatedSecondary, shape = androidx.compose.foundation.shape.CircleShape))
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = SophisticatedTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                Icon(Icons.Filled.Search, contentDescription = "Find", tint = SophisticatedTextMuted, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = { viewModel.undo() }, enabled = uiState.canUndo) {
                Icon(Icons.Filled.Undo, contentDescription = "Undo", tint = if (uiState.canUndo) SophisticatedText else SophisticatedTextMuted.copy(alpha=0.5f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { viewModel.redo() }, enabled = uiState.canRedo) {
                Icon(Icons.Filled.Redo, contentDescription = "Redo", tint = if (uiState.canRedo) SophisticatedText else SophisticatedTextMuted.copy(alpha=0.5f), modifier = Modifier.size(20.dp))
            }
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = SophisticatedTextMuted, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Clear Content") }, onClick = { 
                        viewModel.updateContent("") 
                        expanded = false 
                    })
                    DropdownMenuItem(text = { Text("Revert to Saved") }, onClick = { 
                        viewModel.loadFile(path)
                        expanded = false 
                    })
                }
            }
            
            if (uiState.hasUnsavedChanges) {
                IconButton(onClick = { viewModel.saveFile() }) {
                    Icon(Icons.Filled.Save, contentDescription = "Save", tint = SophisticatedPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
        
        HorizontalDivider(color = SophisticatedSurfaceVariant, thickness = 1.dp)

        if (isSearchExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorBackground)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Find...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurface,
                        unfocusedContainerColor = SophisticatedSurface,
                        focusedBorderColor = SophisticatedPrimary,
                        unfocusedBorderColor = SophisticatedSurfaceVariant
                    ),
                    textStyle = TextStyle(color = SophisticatedText)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { isSearchExpanded = false; searchQuery = "" }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close search", tint = SophisticatedTextMuted)
                }
            }
            HorizontalDivider(color = SophisticatedSurfaceVariant, thickness = 1.dp)
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SophisticatedPrimary)
            }
        } else if (uiState.currentPath == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No file opened", color = SophisticatedTextMuted)
            }
        } else {
            // Editor Content
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    // Line numbers inside the scroll so they stay perfectly in sync
                    val lineCount = uiState.content.count { it == '\n' } + 1
                    val fontSizeSp = viewModel.fontSize.sp
                    val lineHeightSp = (viewModel.fontSize * 1.5).sp
                    val lineHeightDp = with(androidx.compose.ui.platform.LocalDensity.current) { lineHeightSp.toDp() }
                    
                    Column(
                        modifier = Modifier
                            .background(SophisticatedSurface)
                            .padding(end = 1.dp) // Border gap
                            .background(SophisticatedSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(SophisticatedSurface)
                                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                                .height(IntrinsicSize.Min),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = i.toString(),
                                    color = EditorLineNumber,
                                    fontSize = fontSizeSp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.height(lineHeightDp) // Exact match to BasicTextField line height
                                )
                            }
                        }
                    }

                    // TextField
                    BasicTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            color = SophisticatedText,
                            fontSize = fontSizeSp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = lineHeightSp
                        ),
                        cursorBrush = SolidColor(SophisticatedPrimary),
                        visualTransformation = if (viewModel.highContrastCode || searchQuery.isNotEmpty()) {
                             SyntaxHighlightTransformation(viewModel.highContrastCode, searchQuery)
                        } else VisualTransformation.None,
                        modifier = Modifier
                            .weight(1f)
                            .background(EditorBackground)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .horizontalScroll(horizontalScrollState) // Allow long lines
                    )
                }
            }

            // VS Code style Bottom Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(SophisticatedPrimary.copy(alpha = 0.8f))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Close, contentDescription = "Errors", tint = SophisticatedOnPrimary, modifier = Modifier.size(12.dp))
                    Text(" 0 ", style = MaterialTheme.typography.labelSmall, color = SophisticatedOnPrimary, fontSize = 10.sp)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val lang = uiState.fileName.substringAfterLast('.', "").uppercase().takeIf { it.isNotEmpty() } ?: "TXT"
                    Text("UTF-8", style = MaterialTheme.typography.labelSmall, color = SophisticatedOnPrimary, fontSize = 10.sp, modifier = Modifier.padding(end = 16.dp))
                    Text(lang, style = MaterialTheme.typography.labelSmall, color = SophisticatedOnPrimary, fontSize = 10.sp)
                }
            }
        }
    }
}

class SyntaxHighlightTransformation(
    private val useHighContrast: Boolean = true,
    private val searchQuery: String = ""
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = buildAnnotatedString {
            append(text.text)
            
            if (useHighContrast) {
                // Very basic Regex regex for demo premium look
                // Keywords
                val keywordPattern = "\\b(fun|val|var|class|interface|object|if|else|when|return|true|false|null|import|package)\\b".toRegex()
                keywordPattern.findAll(text.text).forEach { result ->
                    addStyle(SpanStyle(color = SophisticatedPrimary), result.range.first, result.range.last + 1)
                }
                
                // Strings
                val stringPattern = "\".*?\"".toRegex()
                stringPattern.findAll(text.text).forEach { result ->
                    addStyle(SpanStyle(color = Color(0xFFCE9178)), result.range.first, result.range.last + 1) // VS Code Orange String Color
                }
                
                // Annotations
                val annotationPattern = "@[a-zA-Z_0-9]+".toRegex()
                annotationPattern.findAll(text.text).forEach { result ->
                    addStyle(SpanStyle(color = Color(0xFFDCDCAA)), result.range.first, result.range.last + 1) // VS Code Yellow Function/Annotation Color
                }
                
                // Comments (Basic single line) //
                val commentPattern = "//.*".toRegex()
                commentPattern.findAll(text.text).forEach { result ->
                    addStyle(SpanStyle(color = Color(0xFF6A9955)), result.range.first, result.range.last + 1) // VS Code Green Comment
                }
            }
            
            if (searchQuery.isNotEmpty()) {
                val searchRegex = Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE)
                searchRegex.findAll(text.text).forEach { result ->
                    addStyle(SpanStyle(background = Color(0xFFFBC02D).copy(alpha = 0.5f), color = Color.Black), result.range.first, result.range.last + 1)
                }
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}
