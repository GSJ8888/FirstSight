package tech.huangsh.firstsight.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import tech.huangsh.firstsight.ui.theme.FirstSightIcons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tech.huangsh.firstsight.R
import tech.huangsh.firstsight.data.model.Contact
import tech.huangsh.firstsight.ui.theme.FirstSightPalette
import tech.huangsh.firstsight.ui.theme.FirstSightStyle

@Composable
fun ContactItem(
    contact: Contact,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isHomeScreen: Boolean = false, // 新增参数标识是否在首页使用
    avatarIndex: Int = 0 // 用于挑选头像渐变色，让相邻联系人视觉上有区分
) {
    Card(
        modifier = modifier
            .let { 
                if (isHomeScreen) {
                    it.fillMaxWidth().aspectRatio(1f) // 首页保持正方形
                } else if (modifier == Modifier.size(50.dp)) {
                    it // 在联系人管理页面中，保持指定大小
                } else {
                    it.fillMaxWidth().height(120.dp) // 其他页面使用固定高度
                }
            }
            // 蓝调投影比默认黑阴影更贴合整体色调
            .shadow(
                elevation = if (isHomeScreen) 8.dp else 2.dp,
                shape = if (isHomeScreen) FirstSightStyle.cardLarge else RoundedCornerShape(12.dp),
                ambientColor = FirstSightStyle.cardShadow(),
                spotColor = FirstSightStyle.cardShadow()
            )
            .clickable { onClick() },
        shape = if (isHomeScreen) FirstSightStyle.cardLarge else RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像 - 在首页中占据大部分空间
            Box(
                modifier = Modifier
                    .let { 
                        if (isHomeScreen) {
                            // 姓名加大后需要更多纵向空间，头像区稍作让步
                            Modifier.fillMaxWidth().weight(0.68f)
                        } else {
                            Modifier.size(if (modifier == Modifier.size(50.dp)) 50.dp else 100.dp)
                        }
                    }
                    .clip(
                        if (isHomeScreen) {
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        } else {
                            CircleShape
                        }
                    )
                    .background(
                        if (isHomeScreen) {
                            Brush.linearGradient(
                                listOf(
                                    FirstSightStyle.avatarGradientColors(avatarIndex).first,
                                    FirstSightStyle.avatarGradientColors(avatarIndex).second
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    FirstSightStyle.avatarGradientColors(avatarIndex).first.copy(alpha = 0.3f),
                                    FirstSightStyle.avatarGradientColors(avatarIndex).second.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
            ) {
                if (contact.avatarUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(contact.avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.contact_avatar),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (isHomeScreen && contact.name.isNotBlank()) {
                    // 没有头像时用姓氏首字，比灰色人形图标更容易辨认
                    Text(
                        text = FirstSightStyle.initialOf(contact.name),
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(if (isHomeScreen) 0.5f else 0.7f)
                            .align(Alignment.Center),
                        tint = if (isHomeScreen) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 姓名 - 只在首页显示，其他页面显示方式不变
            if (isHomeScreen) {
                Text(
                    text = contact.name,
                    // 直接从主题字号取值而不是用 titleMedium：标题类样式在
                    // “小/大字号”设置下的缩放比例不适合姓名，这里固定加大加粗
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(0.32f)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else if (modifier != Modifier.size(50.dp)) { // 在非首页且非联系人管理页面中显示姓名
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactActionBottomSheet(
    contact: Contact,
    isNetworkAvailable: Boolean = true,
    isSimReady: Boolean = true,
    onDismiss: () -> Unit,
    onVideoCall: () -> Unit,
    onVoiceCall: () -> Unit,
    onPhoneCall: (String) -> Unit,
    onCancelCall: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (contact.hasVideoCall) {
                val videoLabel = if (isNetworkAvailable) {
                    stringResource(R.string.wechat_video)
                } else {
                    "${stringResource(R.string.wechat_video)}（无网络）"
                }
                ActionButton(
                    icon = FirstSightIcons.VideoCall,
                    text = videoLabel,
                    backgroundColor = if (isNetworkAvailable) {
                        FirstSightStyle.profileColors(FirstSightPalette.LightVideoStart, FirstSightPalette.DarkVideoStart)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isNetworkAvailable) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    backgroundBrush = if (isNetworkAvailable) FirstSightStyle.videoButtonGradient() else null,
                    onClick = onVideoCall
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            if (contact.hasVoiceCall) {
                val voiceLabel = if (isNetworkAvailable) {
                    stringResource(R.string.wechat_voice)
                } else {
                    "${stringResource(R.string.wechat_voice)}（无网络）"
                }
                ActionButton(
                    icon = Icons.Default.Phone,
                    text = voiceLabel,
                    backgroundColor = if (isNetworkAvailable) {
                        FirstSightStyle.profileColors(FirstSightPalette.LightVoiceStart, FirstSightPalette.DarkVoiceStart)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isNetworkAvailable) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    backgroundBrush = if (isNetworkAvailable) FirstSightStyle.voiceButtonGradient() else null,
                    onClick = onVoiceCall
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            if (contact.hasPhoneCall && !contact.phone.isNullOrBlank()) {
                val callLabel = if (isSimReady) {
                    stringResource(R.string.phone_call)
                } else {
                    "${stringResource(R.string.phone_call)}（未插卡）"
                }
                ActionButton(
                    icon = Icons.Default.Call,
                    text = callLabel,
                    backgroundColor = if (isSimReady) {
                        FirstSightStyle.profileColors(FirstSightPalette.LightCallStart, FirstSightPalette.DarkCallStart)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isSimReady) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    backgroundBrush = if (isSimReady) FirstSightStyle.callButtonGradient() else null,
                    onClick = { contact.phone?.let { onPhoneCall(it) } }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            ActionButton(
                text = stringResource(R.string.cancel),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onCancelCall() }
            )
        }
    }
}

@Composable
fun CommonTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 与页面背景同色：不再用蓝色渐变条，让内容区从顶部就保持统一底色
            .background(FirstSightStyle.pageGradient())
            // 上下留白收紧：标题文字本身有行高，额外 padding 太大会显得空旷
            .padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.2).sp,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun ActionButton(
    icon: ImageVector? = null,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    backgroundBrush: Brush? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(
                elevation = 10.dp,
                shape = FirstSightStyle.cardMedium,
                ambientColor = backgroundColor.copy(alpha = 0.30f),
                spotColor = backgroundColor.copy(alpha = 0.30f)
            )
            .clickable { onClick() },
        shape = FirstSightStyle.cardMedium,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush ?: Brush.linearGradient(listOf(backgroundColor, backgroundColor)))
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(11.dp))
            }
            Text(
                text = text,
                fontSize = 20.sp,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}