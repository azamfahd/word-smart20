package com.example.presentation.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TemplateCard(
    template: TemplateItem,
    isCompact: Boolean,
    onClick: () -> Unit
) {
    val cardWidth = if (template.isLandscape) {
        if (isCompact) 170.dp else 220.dp
    } else {
        if (isCompact) 130.dp else 165.dp
    }
    
    val cardHeight = if (template.isLandscape) {
        if (isCompact) 120.dp else 155.dp
    } else {
        if (isCompact) 180.dp else 225.dp
    }

    Column(
        modifier = Modifier
            .width(cardWidth)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
        ) {
            // Render realistic miniature preview based on template type
            TemplateVisualMock(template = template)

            // Optional Badge (e.g. شائع, مميز, أكاديمي)
            if (template.badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(template.primaryColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = template.badgeText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = template.title,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            fontSize = if (isCompact) 13.sp else 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = template.category.title,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF64748B),
            fontSize = if (isCompact) 11.sp else 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TemplateVisualMock(template: TemplateItem) {
    when (template.previewType) {
        TemplatePreviewType.BLANK -> BlankMock(template.primaryColor)
        TemplatePreviewType.RESUME_SIDEBAR -> ResumeSidebarMock(template.primaryColor, template.secondaryColor, template.accentColor)
        TemplatePreviewType.RESUME_HEADER -> ResumeHeaderMock(template.primaryColor, template.secondaryColor)
        TemplatePreviewType.CERTIFICATE_GOLD -> CertificateGoldMock(template.primaryColor, template.secondaryColor)
        TemplatePreviewType.CERTIFICATE_MODERN -> CertificateModernMock(template.primaryColor, template.secondaryColor)
        TemplatePreviewType.RESEARCH_PAPER -> ResearchPaperMock(template.primaryColor, template.accentColor)
        TemplatePreviewType.STUDY_NOTE -> StudyNoteMock(template.primaryColor, template.secondaryColor)
        TemplatePreviewType.FORMAL_LETTER -> FormalLetterMock(template.primaryColor, template.secondaryColor)
        TemplatePreviewType.COVER_LETTER -> CoverLetterMock(template.primaryColor, template.secondaryColor)
        TemplatePreviewType.INVOICE -> InvoiceMock(template.primaryColor, template.secondaryColor)
        TemplatePreviewType.BUSINESS_REPORT -> BusinessReportMock(template.primaryColor, template.secondaryColor)
    }
}

// 1. Blank
@Composable
private fun BlankMock(primaryColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Blank",
            tint = primaryColor.copy(alpha = 0.7f),
            modifier = Modifier.size(36.dp)
        )
    }
}

// 2. Resume with Side Stripe / Avatar
@Composable
private fun ResumeSidebarMock(primary: Color, secondary: Color, accent: Color) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Colored Left/Right Sidebar strip
        Box(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight()
                .background(primary)
                .padding(4.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Mini avatar
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Mini badges
                Box(modifier = Modifier.size(16.dp, 3.dp).clip(RoundedCornerShape(1.dp)).background(Color.White.copy(alpha = 0.7f)))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.size(14.dp, 3.dp).clip(RoundedCornerShape(1.dp)).background(Color.White.copy(alpha = 0.7f)))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.size(16.dp, 3.dp).clip(RoundedCornerShape(1.dp)).background(Color.White.copy(alpha = 0.7f)))
            }
        }

        // Main content lines
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            // Header Name
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).clip(RoundedCornerShape(2.dp)).background(primary))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(3.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF94A3B8)))
            
            Spacer(modifier = Modifier.height(8.dp))
            // Section 1
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(4.dp).clip(RoundedCornerShape(1.dp)).background(accent))
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFE2E8F0)))
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color(0xFFCBD5E1)))
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFFCBD5E1)))

            Spacer(modifier = Modifier.height(8.dp))
            // Section 2
            Box(modifier = Modifier.fillMaxWidth(0.45f).height(4.dp).clip(RoundedCornerShape(1.dp)).background(accent))
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.fillMaxWidth(0.85f).height(2.dp).background(Color(0xFFCBD5E1)))
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(2.dp).background(Color(0xFFCBD5E1)))
        }
    }
}

// 3. Tech Resume Header
@Composable
private fun ResumeHeaderMock(primary: Color, secondary: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(primary)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                Box(modifier = Modifier.size(50.dp, 6.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.size(35.dp, 3.dp).clip(RoundedCornerShape(1.dp)).background(Color.White.copy(alpha = 0.8f)))
            }
        }

        // Body with two mini columns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.weight(0.6f)) {
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).background(primary))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color(0xFFCBD5E1)))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(4.dp).background(primary))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(0.4f)) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(secondary))
                Spacer(modifier = Modifier.height(3.dp))
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFE2E8F0)))
                Spacer(modifier = Modifier.height(3.dp))
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFE2E8F0)))
            }
        }
    }
}

// 4. Gold Appreciation Certificate (Landscape)
@Composable
private fun CertificateGoldMock(primary: Color, secondary: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .border(2.dp, primary, RoundedCornerShape(2.dp))
            .padding(3.dp)
            .border(1.dp, primary.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
            .background(Color(0xFFFFFDF5))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Gold Seal Badge
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Certificate Title
            Box(modifier = Modifier.size(80.dp, 6.dp).clip(RoundedCornerShape(2.dp)).background(primary))
            Spacer(modifier = Modifier.height(6.dp))
            // Recipient Line
            Box(modifier = Modifier.size(110.dp, 3.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF1E293B)))
            Spacer(modifier = Modifier.height(6.dp))
            // Body text lines
            Box(modifier = Modifier.size(130.dp, 2.dp).background(Color(0xFF94A3B8)))
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.size(100.dp, 2.dp).background(Color(0xFF94A3B8)))
            
            Spacer(modifier = Modifier.weight(1f))
            // Signatures
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(35.dp, 2.dp).background(primary))
                Box(modifier = Modifier.size(35.dp, 2.dp).background(primary))
            }
        }
    }
}

// 5. Modern Blue Certificate
@Composable
private fun CertificateModernMock(primary: Color, secondary: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .border(2.dp, primary, RoundedCornerShape(2.dp))
            .background(Color.White)
            .padding(6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Org Bar
            Box(modifier = Modifier.size(50.dp, 3.dp).background(primary))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(90.dp, 6.dp).background(primary))
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(100.dp, 4.dp).background(Color(0xFF0F172A)))
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(120.dp, 2.dp).background(Color(0xFF94A3B8)))
            
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(30.dp, 2.dp).background(Color(0xFF475569)))
                Box(modifier = Modifier.size(20.dp, 10.dp).border(1.dp, primary, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(30.dp, 2.dp).background(Color(0xFF475569)))
            }
        }
    }
}

// 6. Research Paper
@Composable
private fun ResearchPaperMock(primary: Color, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // University header
        Box(modifier = Modifier.fillMaxWidth(0.6f).height(3.dp).align(Alignment.CenterHorizontally).background(Color(0xFF94A3B8)))
        Spacer(modifier = Modifier.height(6.dp))
        // Title
        Box(modifier = Modifier.fillMaxWidth(0.9f).height(6.dp).align(Alignment.CenterHorizontally).background(primary))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth(0.5f).height(3.dp).align(Alignment.CenterHorizontally).background(Color(0xFF64748B)))
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Abstract Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                .background(Color(0xFFF8FAFC))
                .padding(4.dp)
        ) {
            Column {
                Box(modifier = Modifier.size(30.dp, 3.dp).background(accent))
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFF94A3B8)))
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFF94A3B8)))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Section 1
        Box(modifier = Modifier.fillMaxWidth(0.4f).height(4.dp).background(primary))
        Spacer(modifier = Modifier.height(3.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth(0.7f).height(2.dp).background(Color(0xFFCBD5E1)))
    }
}

// 7. Study Note
@Composable
private fun StudyNoteMock(primary: Color, secondary: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Top Highlighting Ribbon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(secondary)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).background(primary))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Note Card 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFFEF9C3)) // Yellow highlight note
                .padding(4.dp)
        ) {
            Column {
                Box(modifier = Modifier.size(40.dp, 3.dp).background(Color(0xFF854D0E)))
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFA16207)))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Bullet points
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(primary))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFF64748B)))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(primary))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(2.dp).background(Color(0xFF64748B)))
        }
    }
}

// 8. Formal Letter
@Composable
private fun FormalLetterMock(primary: Color, secondary: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Official Letterhead Stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(primary)
                .padding(horizontal = 4.dp, vertical = 3.dp)
        ) {
            Box(modifier = Modifier.size(30.dp, 3.dp).background(Color.White))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date and Ref
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(25.dp, 2.dp).background(Color(0xFF94A3B8)))
            Box(modifier = Modifier.size(35.dp, 2.dp).background(Color(0xFF94A3B8)))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recipient
        Box(modifier = Modifier.fillMaxWidth(0.5f).height(4.dp).background(Color(0xFF1E293B)))
        Spacer(modifier = Modifier.height(3.dp))
        Box(modifier = Modifier.fillMaxWidth(0.4f).height(3.dp).background(Color(0xFF64748B)))

        Spacer(modifier = Modifier.height(8.dp))

        // Body Paragraphs
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth(0.6f).height(2.dp).background(Color(0xFFCBD5E1)))

        Spacer(modifier = Modifier.weight(1f))

        // Signature & Stamp
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Box(modifier = Modifier.size(16.dp, 16.dp).clip(CircleShape).border(1.dp, primary))
            Box(modifier = Modifier.size(40.dp, 2.dp).background(Color(0xFF1E293B)))
        }
    }
}

// 9. Cover Letter
@Composable
private fun CoverLetterMock(primary: Color, secondary: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.6f).height(6.dp).background(primary))
        Spacer(modifier = Modifier.height(3.dp))
        Box(modifier = Modifier.fillMaxWidth(0.4f).height(2.dp).background(Color(0xFF94A3B8)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(primary.copy(alpha = 0.5f)))

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(3.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(3.dp))
        Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFFCBD5E1)))

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(3.dp))
        Box(modifier = Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color(0xFFCBD5E1)))

        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(35.dp, 2.dp).background(Color(0xFF1E293B)))
    }
}

// 10. Invoice with Table
@Composable
private fun InvoiceMock(primary: Color, secondary: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Invoice Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(40.dp, 8.dp).background(primary))
            Box(modifier = Modifier.size(30.dp, 4.dp).background(Color(0xFF94A3B8)))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mini Table Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
        ) {
            // Table Header Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(primary)
            )
            // Row 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White)
            )
            // Row 2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0xFFF8FAFC))
            )
            // Row 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Total Badge
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .align(Alignment.End)
                .clip(RoundedCornerShape(2.dp))
                .background(secondary)
                .padding(2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(primary.copy(alpha = 0.2f)))
        }
    }
}

// 11. Business Report
@Composable
private fun BusinessReportMock(primary: Color, secondary: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.8f).height(6.dp).background(primary))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth(0.5f).height(3.dp).background(Color(0xFF64748B)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))

        Spacer(modifier = Modifier.height(8.dp))

        // Mini bar chart / KPI mock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(modifier = Modifier.size(14.dp, 16.dp).background(primary.copy(alpha = 0.5f)))
            Box(modifier = Modifier.size(14.dp, 24.dp).background(primary))
            Box(modifier = Modifier.size(14.dp, 20.dp).background(primary.copy(alpha = 0.8f)))
            Box(modifier = Modifier.size(14.dp, 28.dp).background(primary))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFCBD5E1)))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(Color(0xFFCBD5E1)))
    }
}
