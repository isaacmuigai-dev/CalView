package com.example.calview.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter

/**
 * Terms and Conditions screen displaying the End User License Agreement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Terms and Conditions",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Title
            Text(
                text = "CALVIEW END USER LICENSE AGREEMENT",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Last Updated: December 31, 2024",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Introduction
            EulaSection(
                content = """CalView ("the Application") made available through the Google Play Store and Apple App Store is licensed, not sold, to you. Your license to the Application is subject to your prior acceptance of this End User License Agreement ("EULA"). Your license to the Application under this EULA is granted by CalView ("Licensor"). The Licensor reserves all rights in and to the Application not expressly granted to you under this EULA."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section A
            EulaSectionWithTitle(
                title = "a. Scope of License",
                content = """Licensor grants to you a nontransferable license to use the Application on any Android or iOS devices that you own or control and as permitted by the respective app store's usage rules. The terms of this EULA will govern any content, materials, or services accessible from or purchased within the Application as well as upgrades provided by Licensor that replace or supplement the original Application.

You may not distribute or make the Application available over a network where it could be used by multiple devices at the same time. You may not transfer, redistribute or sublicense the Application and, if you sell your device to a third party, you must remove the Application from the device before doing so.

You may not copy (except as permitted by this license), reverse-engineer, disassemble, attempt to derive the source code of, modify, or create derivative works of the Application, any updates, or any part thereof (except as and only to the extent that any foregoing restriction is prohibited by applicable law or to the extent as may be permitted by the licensing terms governing use of any open-sourced components included with the Application)."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section B
            EulaSectionWithTitle(
                title = "b. Consent to Use of Data",
                content = """You agree that Licensor may collect and use technical data and related information—including but not limited to technical information about your device, system and application software, and peripherals—that is gathered periodically to facilitate the provision of software updates, product support, and other services to you (if any) related to the Application.

Additionally, the Application collects nutritional and health-related data that you voluntarily input, including but not limited to: food logs, calorie intake, macronutrient data (protein, carbohydrates, fats), weight measurements, and fitness goals.

Licensor may use this information, as long as it is in a form that does not personally identify you, to improve its products or to provide services or technologies to you. Your personal health data will be handled in accordance with our Privacy Policy."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section C
            EulaSectionWithTitle(
                title = "c. Termination",
                content = """This EULA is effective until terminated by you or Licensor. Your rights under this EULA will terminate automatically if you fail to comply with any of its terms. Upon termination, you must cease all use of the Application and delete all copies from your devices."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section D
            EulaSectionWithTitle(
                title = "d. External Services",
                content = """The Application may enable access to Licensor's and/or third-party services and websites (collectively and individually, "External Services"), including but not limited to Google Health Connect, food databases, and nutritional APIs. You agree to use the External Services at your sole risk.

Licensor is not responsible for examining or evaluating the content or accuracy of any third-party External Services, and shall not be liable for any such third-party External Services.

Data displayed by the Application or External Service, including but not limited to nutritional information, calorie counts, and health recommendations, is for general informational purposes only and is not guaranteed by Licensor or its agents. This information should not be used as a substitute for professional medical advice, diagnosis, or treatment.

You will not use the External Services in any manner that is inconsistent with the terms of this EULA or that infringes the intellectual property rights of Licensor or any third party. Licensor reserves the right to change, suspend, remove, disable or impose access restrictions or limits on any External Services at any time without notice or liability to you."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section E
            EulaSectionWithTitle(
                title = "e. Health Disclaimer",
                content = """THE APPLICATION IS DESIGNED TO ASSIST WITH CALORIE TRACKING AND NUTRITIONAL AWARENESS. IT IS NOT INTENDED TO DIAGNOSE, TREAT, CURE, OR PREVENT ANY DISEASE OR HEALTH CONDITION.

The nutritional information and recommendations provided by the Application are based on general guidelines and may not be suitable for your specific health needs. Always consult with a qualified healthcare professional before making any significant changes to your diet or exercise routine.

The Application's AI-powered food scanning feature provides estimates based on image analysis and may not be 100% accurate. Users should verify nutritional information when accuracy is critical for medical or dietary requirements."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section F
            EulaSectionWithTitle(
                title = "f. No Warranty",
                content = """YOU EXPRESSLY ACKNOWLEDGE AND AGREE THAT USE OF THE APPLICATION IS AT YOUR SOLE RISK. TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, THE APPLICATION AND ANY SERVICES PERFORMED OR PROVIDED BY THE APPLICATION ARE PROVIDED "AS IS" AND "AS AVAILABLE," WITH ALL FAULTS AND WITHOUT WARRANTY OF ANY KIND.

LICENSOR HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS WITH RESPECT TO THE APPLICATION AND ANY SERVICES, EITHER EXPRESS, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES AND/OR CONDITIONS OF MERCHANTABILITY, OF SATISFACTORY QUALITY, OF FITNESS FOR A PARTICULAR PURPOSE, OF ACCURACY, OF QUIET ENJOYMENT, AND OF NONINFRINGEMENT OF THIRD-PARTY RIGHTS.

NO ORAL OR WRITTEN INFORMATION OR ADVICE GIVEN BY LICENSOR OR ITS AUTHORIZED REPRESENTATIVE SHALL CREATE A WARRANTY. SHOULD THE APPLICATION OR SERVICES PROVE DEFECTIVE, YOU ASSUME THE ENTIRE COST OF ALL NECESSARY SERVICING, REPAIR, OR CORRECTION.

SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OF IMPLIED WARRANTIES OR LIMITATIONS ON APPLICABLE STATUTORY RIGHTS OF A CONSUMER, SO THE ABOVE EXCLUSION AND LIMITATIONS MAY NOT APPLY TO YOU."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section G
            EulaSectionWithTitle(
                title = "g. Limitation of Liability",
                content = """TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT SHALL LICENSOR BE LIABLE FOR PERSONAL INJURY OR ANY INCIDENTAL, SPECIAL, INDIRECT, OR CONSEQUENTIAL DAMAGES WHATSOEVER, INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOSS OF PROFITS, LOSS OF DATA, BUSINESS INTERRUPTION, OR ANY OTHER COMMERCIAL DAMAGES OR LOSSES, ARISING OUT OF OR RELATED TO YOUR USE OF OR INABILITY TO USE THE APPLICATION.

THIS LIMITATION APPLIES REGARDLESS OF THE THEORY OF LIABILITY (CONTRACT, TORT, OR OTHERWISE) AND EVEN IF LICENSOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

IN NO EVENT SHALL LICENSOR'S TOTAL LIABILITY TO YOU FOR ALL DAMAGES (OTHER THAN AS MAY BE REQUIRED BY APPLICABLE LAW IN CASES INVOLVING PERSONAL INJURY) EXCEED THE AMOUNT OF FIFTY DOLLARS ($50.00).

SOME JURISDICTIONS DO NOT ALLOW THE LIMITATION OF LIABILITY FOR PERSONAL INJURY, OR OF INCIDENTAL OR CONSEQUENTIAL DAMAGES, SO THIS LIMITATION MAY NOT APPLY TO YOU."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section H
            EulaSectionWithTitle(
                title = "h. Governing Law",
                content = """This Agreement and the relationship between you and CalView shall be governed by the laws of the Republic of Kenya, excluding its conflicts of law provisions.

If you are a citizen of any European Union country or Switzerland, Norway or Iceland, the governing law and forum shall be the laws and courts of your usual place of residence.

Specifically excluded from application to this Agreement is that law known as the United Nations Convention on the International Sale of Goods."""
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Section I
            EulaSectionWithTitle(
                title = "i. Contact Information",
                content = """If you have any questions about this EULA, please contact us at:

Email: support@calview.app

By using CalView, you acknowledge that you have read this agreement, understand it, and agree to be bound by its terms and conditions."""
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun EulaSection(content: String) {
    Text(
        text = content,
        fontFamily = Inter,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun EulaSectionWithTitle(title: String, content: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(title)
            }
            append("\n\n")
            append(content)
        },
        fontFamily = Inter,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}
