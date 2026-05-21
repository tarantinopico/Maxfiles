package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorLineNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    path: String?,
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(path) {
        if (uiState.currentPath != path) {
            viewModel.loadFile(path)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { 
                    Column {
                        Text(
                            text = uiState.fileName + if (uiState.hasUnsavedChanges) " (Edited)" else "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        uiState.currentPath?.let { 
                            if (it.contains("/")) {
                                Text(
                                    text = ".../" + it.split("/").takeLast(2).first(), 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { /* TODO: More Actions */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.height(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = { /* TODO undo */ }) {
                            Icon(Icons.Filled.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { /* TODO redo */ }) {
                            Icon(Icons.Filled.Redo, contentDescription = "Redo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = "UTF-8 • Kotlin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState.hasUnsavedChanges) {
                FloatingActionButton(
                    onClick = { viewModel.saveFile() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(EditorBackground)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.currentPath == null) {
                Text("No file selected", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
            } else {
                Row(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    // Line numbers
                    val lineCount = uiState.content.count { it == '\n' } + 1
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .width(32.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = i.toString(),
                                    color = EditorLineNumber,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.height(20.dp) // Approximate height to match line height
                                )
                            }
                        }
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                    
                    // Code Editor
                    BasicTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = SyntaxHighlightTransformation(),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }
            if (uiState.isSaving) {
                LinearProgressIndicator(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth())
            }
        }
    }
}

class SyntaxHighlightTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            highlightSyntax(text.text),
            OffsetMapping.Identity
        )
    }

    private fun highlightSyntax(text: String): AnnotatedString {
        return buildAnnotatedString {
            append(text)
            
            // Simple keyword matching for demo purposes
            val keywords = listOf("val", "var", "fun", "class", "interface", "object", "return", "if", "else", "true", "false", "for", "while", "import", "package")
            val colors = mapOf(
                "keyword" to Color(0xFFCFBCFF), 
                "string" to Color(0xFF10B981),
                "number" to Color(0xFFF59E0B),
                "comment" to Color(0xFF6B7280)
            )

            // Extremely basic regex mapping - in real app use a proper lexer
            val wordRegex = "\\b(\\w+)\\b".toRegex()
            wordRegex.findAll(text).forEach { result ->
                if (keywords.contains(result.value)) {
                    addStyle(SpanStyle(color = colors["keyword"]!!, fontWeight = FontWeight.Bold), result.range.first, result.range.last + 1)
                } else if (result.value.matches("\\d+".toRegex())) {
                    addStyle(SpanStyle(color = colors["number"]!!), result.range.first, result.range.last + 1)
                }
            }
            
            val stringRegex = "\".*?\"".toRegex()
            stringRegex.findAll(text).forEach { result ->
                addStyle(SpanStyle(color = colors["string"]!!), result.range.first, result.range.last + 1)
            }
            
            val commentRegex = "//.*".toRegex()
            commentRegex.findAll(text).forEach { result ->
                addStyle(SpanStyle(color = colors["comment"]!!), result.range.first, result.range.last + 1)
            }
        }
    }
}
