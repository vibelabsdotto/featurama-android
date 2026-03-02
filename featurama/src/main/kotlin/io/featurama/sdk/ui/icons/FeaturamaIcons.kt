package io.featurama.sdk.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp

@Composable
internal fun CloseIcon(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.width
        val m = s * 0.2f
        val stroke = Stroke(width = s * 0.12f, cap = StrokeCap.Round)
        drawLine(color, Offset(m, m), Offset(s - m, s - m), strokeWidth = stroke.width)
        drawLine(color, Offset(s - m, m), Offset(m, s - m), strokeWidth = stroke.width)
    }
}

@Composable
internal fun PlusIcon(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.width
        val c = s / 2f
        val m = s * 0.2f
        val sw = s * 0.12f
        drawLine(color, Offset(m, c), Offset(s - m, c), strokeWidth = sw)
        drawLine(color, Offset(c, m), Offset(c, s - m), strokeWidth = sw)
    }
}

@Composable
internal fun ChevronUpIcon(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.width
        val path = Path().apply {
            moveTo(s * 0.25f, s * 0.6f)
            lineTo(s * 0.5f, s * 0.35f)
            lineTo(s * 0.75f, s * 0.6f)
        }
        drawPath(path, color, style = Stroke(width = s * 0.12f, cap = StrokeCap.Round))
    }
}

// SVG path data from the original featurama-logo.svg
// viewBox: 0 0 1237 1150, all paths have transform translate(-772, -217)
private const val FRONT_RECT =
    "M976.216 587.503C1009.58 585.991 1049.94 586.977 1083.57 587.07L1275.48 587.435L1420.48 587.053C1458.4 586.967 1499.87 581.439 1532.53 600.086C1550.29 610.227 1575.43 638.878 1578.43 658.989C1581.14 677.166 1579.89 704.703 1579.92 723.502L1580.18 856.582L1579.88 1065.02L1579.89 1126.27C1579.91 1140.29 1579.69 1153.1 1579 1167.05C1577.11 1205.94 1541.13 1244.43 1503.76 1252.12C1490.94 1257.34 1395.83 1255.34 1375.93 1255.33L1139.26 1255.18L1046.67 1255.14C1030.16 1255.19 1011.12 1256.3 994.901 1255.04C954.868 1251.93 919.84 1227.22 907.562 1189.05C904.631 1179.78 902.917 1170.16 902.464 1160.45C901.131 1132.87 902.337 1095.66 902.323 1067.36L902.207 882.692L902.065 746.608C901.989 722.157 901.556 696.859 902.309 672.474C902.526 665.423 909.179 643.888 912.714 637.573C926.765 612.475 949.211 595.924 976.216 587.503Z"

private const val CHECKMARK =
    "M1234.31 834.225C1242.99 833.096 1250.03 834.093 1256.47 840.562C1285.2 869.299 1313.15 897.299 1341.92 926.181C1353.88 938.185 1359.7 949.351 1346.57 965.125C1340.28 972.687 1325.58 972.709 1317.83 966.726C1307.8 958.971 1298.67 949.168 1289.48 939.938L1243.51 893.646C1242.2 892.38 1242.35 892.642 1240.69 891.761C1231.15 895.291 1177.38 958.69 1162.65 967.889C1156.91 971.475 1149.69 971.922 1143.76 970.012C1137.75 968.069 1132.79 963.756 1130.03 958.071C1126.98 952.005 1126.71 944.916 1129.29 938.637C1131.85 932.332 1141.78 923.416 1146.79 918.395C1173.1 892.021 1199.26 865.457 1225.77 839.297C1228.13 836.964 1231.28 835.477 1234.31 834.225Z"

private const val BACK_RECT =
    "M1287.39 305.409C1294.07 303.682 1330.91 304.762 1339.52 304.765L1473.03 304.829L1697.06 304.801L1761.73 304.728C1774.46 304.667 1789.13 304.265 1801.74 305.104C1815.98 306.052 1832.95 313.176 1844.59 321.407C1863.2 334.564 1879.33 357.576 1883.08 380.334C1884.46 388.685 1884.05 402.776 1884.05 411.696L1884.01 460.949L1884.04 627.28L1883.79 814.37L1883.69 868.473C1883.59 897.171 1883.64 909.619 1865.21 934.491C1849.65 955.171 1826.47 968.781 1800.83 972.297C1790.2 972.922 1780.13 972.713 1769.5 972.53L1769.61 664.297L1769.17 576.244C1769 559.451 1769.55 534.547 1766.31 518.855C1760.8 492.191 1745.54 459.548 1726.25 440.621C1706.31 421.065 1677.77 405.328 1650.3 400.322C1635.79 397.679 1613.76 398.582 1598.56 398.605L1517.45 398.743L1205.48 398.402C1205.41 353.124 1242.25 310.409 1287.39 305.409Z"

private const val MIDDLE_RECT =
    "M1071.94 502.628C1088.96 468.642 1118.12 446.443 1157.26 445.55C1186.11 444.892 1215.24 445.499 1244.12 445.526L1415.23 445.561L1559.42 445.509C1581.31 445.48 1615.4 444.327 1635.81 445.835C1647.19 446.704 1658.29 449.776 1668.5 454.882C1688.69 465.147 1709.98 486.032 1717.13 507.966C1723.04 526.083 1720.24 613.073 1720.23 638.58L1720.19 858.202L1720.42 969.791C1720.46 988.808 1720.81 1008.64 1720.13 1027.63C1719.68 1040.3 1714.51 1052.47 1708.61 1063.64C1697.2 1085.28 1677.71 1101.52 1654.38 1108.84C1644.57 1111.82 1637.9 1112.15 1627.8 1112.18C1627.27 1059.9 1627.29 1007.62 1627.85 955.339L1627.39 781.442C1627.41 754.322 1627.26 727.202 1626.96 700.084C1626.89 686.934 1627.38 661.652 1625.05 649.66C1620.04 623.99 1598.84 593.812 1580.18 575.454C1561.24 556.823 1533.95 547.615 1508.54 540.959C1492.81 538.616 1476.96 539.292 1461.03 539.283C1437.79 539.412 1414.56 539.318 1391.32 538.998C1339.72 541.009 1285.73 537.937 1233.86 539.226C1213.45 539.734 1187.98 540.344 1167.72 539.097C1140.38 540.861 1094.14 539.511 1065.29 539.514C1065.66 524.744 1065.91 516.457 1071.94 502.628Z"

private fun parseSvgPath(pathData: String): Path =
    PathParser().parsePathString(pathData).toPath()

@Composable
internal fun FeaturamaLogoIcon(size: Dp, color: Color) {
    val paths = remember {
        listOf(
            parseSvgPath(BACK_RECT),
            parseSvgPath(MIDDLE_RECT),
            parseSvgPath(FRONT_RECT),
            parseSvgPath(CHECKMARK),
        )
    }

    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.width
        val scale = s / 1237f

        withTransform({
            scale(scale, scale)
            translate(-772f, -217f)
        }) {
            drawPath(paths[0], color) // Back rect
            drawPath(paths[1], color) // Middle rect
            drawPath(paths[2], color) // Front rect
            drawPath(paths[3], Color.White) // Checkmark
        }
    }
}

@Composable
internal fun SendIcon(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.width
        val sw = s * 0.1f
        drawLine(color, Offset(s * 0.25f, s * 0.75f), Offset(s * 0.7f, s * 0.3f), strokeWidth = sw)
        drawLine(color, Offset(s * 0.7f, s * 0.3f), Offset(s * 0.45f, s * 0.3f), strokeWidth = sw)
        drawLine(color, Offset(s * 0.7f, s * 0.3f), Offset(s * 0.7f, s * 0.55f), strokeWidth = sw)
    }
}
