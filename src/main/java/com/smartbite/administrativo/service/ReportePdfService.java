package com.smartbite.administrativo.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.smartbite.administrativo.model.Compra;
import com.smartbite.administrativo.model.ItemInventario;
import com.smartbite.administrativo.model.Producto;
import com.smartbite.administrativo.model.Usuario;
import com.smartbite.administrativo.repository.CompraRepository;
import com.smartbite.administrativo.repository.ItemInventarioRepository;
import com.smartbite.administrativo.repository.ProductoRepository;
import com.smartbite.administrativo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportePdfService {

    private final ProductoRepository productoRepository;
    private final ItemInventarioRepository itemInventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompraRepository compraRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Colores profesionales
    private static final BaseColor COLOR_PRIMARY = new BaseColor(41, 128, 185);
    private static final BaseColor COLOR_SECONDARY = new BaseColor(52, 73, 94);
    private static final BaseColor COLOR_ACCENT = new BaseColor(46, 204, 113);
    private static final BaseColor COLOR_WARNING = new BaseColor(231, 76, 60);
    private static final BaseColor COLOR_BORDER = new BaseColor(189, 195, 199);

    // Fuentes
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_SECONDARY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font FOOTER_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, BaseColor.GRAY);

    // ============================================
    // LOGO + TÍTULO ALINEADOS
    // ============================================
    private void addLogoAndTitle(Document document, String logoName, String titleText) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{20, 60, 20});
        headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Celda del logo (izquierda)
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        try {
            URL logoUrl = getClass().getClassLoader().getResource("static/images/" + logoName);
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(50, 50);
                logoCell.addElement(logo);
            } else {
                Paragraph emojiText = new Paragraph(getEmojiForReport(logoName),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24));
                logoCell.addElement(emojiText);
            }
        } catch (Exception e) {
            Paragraph emojiText = new Paragraph(getEmojiForReport(logoName),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24));
            logoCell.addElement(emojiText);
        }

        // Celda del título (centro)
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph title = new Paragraph(titleText, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        titleCell.addElement(title);

        // Celda vacía derecha
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);

        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        headerTable.addCell(emptyCell);

        document.add(headerTable);
        document.add(new Paragraph(" "));
    }

    private String getEmojiForReport(String logoName) {
        if (logoName.contains("productos")) return "🍽️";
        if (logoName.contains("inventario")) return "📦";
        if (logoName.contains("usuarios")) return "👥";
        if (logoName.contains("compras")) return "🛒";
        return "📋";
    }

    // ============================================
    // 1. REPORTE DE PRODUCTOS
    // ============================================
    public byte[] generarReporteProductos() throws DocumentException, IOException {
        List<Producto> productos = productoRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setPageEvent(new PageNumberEvent());

        document.open();

        // Logo + Título
        addLogoAndTitle(document, "logo-productos.png", "INFORME DE PRODUCTOS");

        // Descripción académica
        Paragraph description = new Paragraph(
                "El presente informe detalla el catálogo completo de productos disponibles en el sistema. " +
                        "Incluye información relevante como identificación, denominación comercial, precio unitario, " +
                        "estado de disponibilidad y categoría asociada. Este reporte es fundamental para la gestión " +
                        "del menú y la planificación de compras.",
                NORMAL_FONT
        );
        description.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        document.add(description);
        document.add(new Paragraph(" "));

        // Fecha de generación
        Paragraph date = new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FORMATTER), FOOTER_FONT);
        date.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(date);
        document.add(new Paragraph(" "));

        // Tabla de productos
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 30, 20, 15, 25});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // Headers
        String[] headers = {"ID", "Nombre", "Precio", "Disponible", "Categoría"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, HEADER_FONT));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Datos
        for (Producto p : productos) {
            table.addCell(createCell(String.valueOf(p.getId()), Element.ALIGN_CENTER));
            table.addCell(createCell(p.getNombre(), Element.ALIGN_LEFT));
            table.addCell(createCell("$" + String.format("%,.0f", p.getPrecio()), Element.ALIGN_RIGHT));

            PdfPCell dispCell = createCell(p.getDisponible() ? "Disponible" : "No disponible", Element.ALIGN_CENTER);
            dispCell.setBackgroundColor(p.getDisponible() ? COLOR_ACCENT : COLOR_WARNING);
            dispCell.setPadding(5);
            table.addCell(dispCell);

            table.addCell(createCell(p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría", Element.ALIGN_LEFT));
        }

        document.add(table);

        // Estadísticas
        addProductStatistics(document, productos);
        addFooter(document);

        document.close();
        return out.toByteArray();
    }

    // ============================================
    // 2. REPORTE DE INVENTARIO
    // ============================================
    public byte[] generarReporteInventario() throws DocumentException, IOException {
        List<ItemInventario> items = itemInventarioRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);

        document.open();

        // Logo + Título
        addLogoAndTitle(document, "logo-inventario.png", "INFORME DE INVENTARIO");

        // Descripción académica
        Paragraph description = new Paragraph(
                "El presente informe presenta el estado actual del inventario de insumos y materias primas. " +
                        "Para cada ítem se detalla la cantidad disponible, el nivel mínimo de stock definido y su estado operativo. " +
                        "Este análisis permite identificar productos con riesgo de desabastecimiento y facilita la planificación " +
                        "de órdenes de compra para mantener la continuidad operativa.",
                NORMAL_FONT
        );
        description.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        document.add(description);
        document.add(new Paragraph(" "));

        // Fecha
        Paragraph date = new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FORMATTER), FOOTER_FONT);
        date.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(date);
        document.add(new Paragraph(" "));

        // Tabla de inventario
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8, 25, 12, 12, 12, 15, 16});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        String[] headers = {"ID", "Producto", "Stock Actual", "Stock Mínimo", "Unidad", "Estado", "Valor Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, HEADER_FONT));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        int stockBajoCount = 0;
        double valorTotalInventario = 0;

        for (ItemInventario item : items) {
            table.addCell(createCell(String.valueOf(item.getId()), Element.ALIGN_CENTER));
            table.addCell(createCell(item.getNombre(), Element.ALIGN_LEFT));
            table.addCell(createCell(String.valueOf(item.getStockActual()), Element.ALIGN_CENTER));
            table.addCell(createCell(String.valueOf(item.getStockMinimo()), Element.ALIGN_CENTER));
            table.addCell(createCell(item.getUnidadMedida(), Element.ALIGN_CENTER));

            PdfPCell estadoCell;
            if (item.getStockActual() <= item.getStockMinimo()) {
                estadoCell = createCell("⚠️ Stock Bajo", Element.ALIGN_CENTER);
                estadoCell.setBackgroundColor(COLOR_WARNING);
                stockBajoCount++;
            } else {
                estadoCell = createCell("✓ Normal", Element.ALIGN_CENTER);
                estadoCell.setBackgroundColor(COLOR_ACCENT);
            }
            estadoCell.setPadding(5);
            table.addCell(estadoCell);

            double valorItem = item.getCostoUnitario() != null ? item.getStockActual() * item.getCostoUnitario() : 0;
            valorTotalInventario += valorItem;
            table.addCell(createCell("$" + String.format("%,.2f", valorItem), Element.ALIGN_RIGHT));
        }

        document.add(table);

        // Estadísticas de inventario
        addInventoryStatistics(document, items, stockBajoCount, valorTotalInventario);
        addFooter(document);

        document.close();
        return out.toByteArray();
    }

    // ============================================
    // 3. REPORTE DE USUARIOS
    // ============================================
    public byte[] generarReporteUsuarios() throws DocumentException, IOException {
        List<Usuario> usuarios = usuarioRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Logo + Título
        addLogoAndTitle(document, "logo-usuarios.png", "INFORME DE USUARIOS");

        // Descripción académica
        Paragraph description = new Paragraph(
                "El presente informe consolida la información del personal que opera el sistema, " +
                        "detallando su identificación, datos de contacto, rol asignado, sucursal de adscripción y estado laboral. " +
                        "Este reporte es fundamental para la administración del talento humano y el control de accesos al sistema.",
                NORMAL_FONT
        );
        description.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        document.add(description);
        document.add(new Paragraph(" "));

        // Fecha
        Paragraph date = new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FORMATTER), FOOTER_FONT);
        date.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(date);
        document.add(new Paragraph(" "));

        // Tabla de usuarios
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8, 20, 22, 15, 20, 15});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        String[] headers = {"ID", "Nombre", "Email", "Rol", "Sucursal", "Estado"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, HEADER_FONT));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        int activos = 0;
        for (Usuario u : usuarios) {
            table.addCell(createCell(String.valueOf(u.getId()), Element.ALIGN_CENTER));
            table.addCell(createCell(u.getNombre(), Element.ALIGN_LEFT));
            table.addCell(createCell(u.getEmail(), Element.ALIGN_LEFT));
            table.addCell(createCell(u.getRol() != null ? u.getRol().getNombre().name() : "Sin rol", Element.ALIGN_LEFT));
            table.addCell(createCell(u.getSucursal() != null ? u.getSucursal().getNombre() : "Sin sucursal", Element.ALIGN_LEFT));

            PdfPCell estadoCell = createCell(u.getActivo() ? "Activo" : "Inactivo", Element.ALIGN_CENTER);
            estadoCell.setBackgroundColor(u.getActivo() ? COLOR_ACCENT : COLOR_WARNING);
            estadoCell.setPadding(5);
            table.addCell(estadoCell);

            if (u.getActivo()) activos++;
        }

        document.add(table);

        // Estadísticas de usuarios
        addUserStatistics(document, usuarios, activos);
        addFooter(document);

        document.close();
        return out.toByteArray();
    }

    // ============================================
    // 4. REPORTE DE COMPRAS
    // ============================================
    public byte[] generarReporteCompras() throws DocumentException, IOException {
        List<Compra> compras = compraRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Logo + Título
        addLogoAndTitle(document, "logo-compras.png", "INFORME DE COMPRAS");

        // Descripción académica
        Paragraph description = new Paragraph(
                "El presente informe detalla las transacciones de adquisición de insumos realizadas a proveedores. " +
                        "Incluye información cronológica, montos involucrados, proveedores y el estado actual de cada orden. " +
                        "Este análisis permite evaluar el comportamiento de gastos y la eficiencia del proceso de abastecimiento.",
                NORMAL_FONT
        );
        description.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        document.add(description);
        document.add(new Paragraph(" "));

        // Fecha
        Paragraph date = new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FORMATTER), FOOTER_FONT);
        date.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(date);
        document.add(new Paragraph(" "));

        // Tabla de compras
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8, 20, 32, 20, 20});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        String[] headers = {"ID", "Fecha", "Proveedor", "Total", "Estado"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, HEADER_FONT));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        double totalCompras = 0;
        int completadas = 0;
        int pendientes = 0;

        for (Compra c : compras) {
            table.addCell(createCell(String.valueOf(c.getId()), Element.ALIGN_CENTER));
            table.addCell(createCell(c.getFechaCompra() != null ? c.getFechaCompra().format(FORMATTER) : "-", Element.ALIGN_CENTER));
            table.addCell(createCell(c.getProvedor() != null ? c.getProvedor().getNombre() : "-", Element.ALIGN_LEFT));
            table.addCell(createCell("$" + String.format("%,.0f", c.getTotal()), Element.ALIGN_RIGHT));

            PdfPCell estadoCell = createCell(c.getEstado().toString(), Element.ALIGN_CENTER);
            switch (c.getEstado().toString()) {
                case "COMPLETADA":
                    estadoCell.setBackgroundColor(COLOR_ACCENT);
                    completadas++;
                    break;
                case "CANCELADA":
                    estadoCell.setBackgroundColor(COLOR_WARNING);
                    break;
                default:
                    pendientes++;
                    break;
            }
            estadoCell.setPadding(5);
            table.addCell(estadoCell);

            totalCompras += c.getTotal();
        }

        document.add(table);

        // Estadísticas de compras
        addPurchaseStatistics(document, compras.size(), totalCompras, completadas, pendientes);
        addFooter(document);

        document.close();
        return out.toByteArray();
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private PdfPCell createCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, NORMAL_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);
        cell.setBorderColor(COLOR_BORDER);
        return cell;
    }

    private void addProductStatistics(Document document, List<Producto> productos) throws DocumentException {
        long disponibles = productos.stream().filter(Producto::getDisponible).count();
        double precioPromedio = productos.stream().mapToDouble(Producto::getPrecio).average().orElse(0);
        double precioMaximo = productos.stream().mapToDouble(Producto::getPrecio).max().orElse(0);
        double precioMinimo = productos.stream().mapToDouble(Producto::getPrecio).min().orElse(0);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("ANÁLISIS ESTADÍSTICO DE PRODUCTOS", SUBTITLE_FONT));
        document.add(new Paragraph(" "));

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(60);
        statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        addStatRow(statsTable, "Total de productos:", String.valueOf(productos.size()));
        addStatRow(statsTable, "Productos disponibles:", String.valueOf(disponibles));
        addStatRow(statsTable, "Productos no disponibles:", String.valueOf(productos.size() - disponibles));
        addStatRow(statsTable, "Precio promedio:", "$" + String.format("%,.0f", precioPromedio));
        addStatRow(statsTable, "Precio máximo:", "$" + String.format("%,.0f", precioMaximo));
        addStatRow(statsTable, "Precio mínimo:", "$" + String.format("%,.0f", precioMinimo));

        document.add(statsTable);
    }

    private void addInventoryStatistics(Document document, List<ItemInventario> items, int stockBajoCount, double valorTotal) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph("ANÁLISIS ESTADÍSTICO DEL INVENTARIO", SUBTITLE_FONT));
        document.add(new Paragraph(" "));

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(60);
        statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        addStatRow(statsTable, "Total de ítems en inventario:", String.valueOf(items.size()));
        addStatRow(statsTable, "Ítems con stock bajo:", String.valueOf(stockBajoCount));
        addStatRow(statsTable, "Ítems con stock óptimo:", String.valueOf(items.size() - stockBajoCount));
        addStatRow(statsTable, "Valor total del inventario:", "$" + String.format("%,.2f", valorTotal));

        document.add(statsTable);

        if (stockBajoCount > 0) {
            Paragraph recommendation = new Paragraph(
                    "⚠️ Recomendación: Se recomienda generar órdenes de compra para los " + stockBajoCount +
                            " ítems que presentan niveles de stock por debajo del mínimo establecido para evitar desabastecimiento.",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_WARNING)
            );
            recommendation.setAlignment(Paragraph.ALIGN_LEFT);
            document.add(new Paragraph(" "));
            document.add(recommendation);
        }
    }

    private void addUserStatistics(Document document, List<Usuario> usuarios, int activos) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph("ANÁLISIS ESTADÍSTICO DE USUARIOS", SUBTITLE_FONT));
        document.add(new Paragraph(" "));

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(60);
        statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        addStatRow(statsTable, "Total de usuarios registrados:", String.valueOf(usuarios.size()));
        addStatRow(statsTable, "Usuarios activos:", String.valueOf(activos));
        addStatRow(statsTable, "Usuarios inactivos:", String.valueOf(usuarios.size() - activos));

        document.add(statsTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Distribución por rol:", BOLD_FONT));

        var rolesCount = usuarios.stream()
                .filter(u -> u.getRol() != null)
                .collect(Collectors.groupingBy(u -> u.getRol().getNombre().name(), Collectors.counting()));

        for (var entry : rolesCount.entrySet()) {
            document.add(new Paragraph("  • " + entry.getKey() + ": " + entry.getValue() + " usuarios", NORMAL_FONT));
        }
    }

    private void addPurchaseStatistics(Document document, int totalCompras, double totalInvertido, int completadas, int pendientes) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph("ANÁLISIS ESTADÍSTICO DE COMPRAS", SUBTITLE_FONT));
        document.add(new Paragraph(" "));

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(60);
        statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        addStatRow(statsTable, "Total de órdenes de compra:", String.valueOf(totalCompras));
        addStatRow(statsTable, "Compras completadas:", String.valueOf(completadas));
        addStatRow(statsTable, "Compras pendientes:", String.valueOf(pendientes));
        addStatRow(statsTable, "Total invertido:", "$" + String.format("%,.0f", totalInvertido));
        addStatRow(statsTable, "Ticket promedio por compra:", "$" + String.format("%,.0f", totalCompras > 0 ? totalInvertido / totalCompras : 0));

        document.add(statsTable);
    }

    private void addStatRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);

        PdfPCell valueCell = new PdfPCell(new Paragraph(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
                "Este informe es generado automáticamente por SmartBite. " +
                        "Si requiere información adicional o aclaraciones, contacte al administrador del sistema.",
                FOOTER_FONT
        );
        footer.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(footer);
    }

    // ============================================
    // NUMERACIÓN DE PÁGINAS
    // ============================================
    class PageNumberEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase("Página " + writer.getPageNumber(), FOOTER_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}