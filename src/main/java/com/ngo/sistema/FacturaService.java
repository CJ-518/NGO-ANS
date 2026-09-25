package com.ngo.sistema;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;

/**
 * Genera una factura simple en PDF a partir de una {@link Venta}. No es un comprobante
 * fiscal timbrado por la SET: es un recibo de referencia para el cliente y para el
 * registro interno del Portal de Ventas.
 *
 * Se usan las fuentes estándar (Helvetica) con codificación limitada, así que los textos
 * se pasan sin tildes/ñ (ver {@link #limpiar(String)}) para evitar errores al dibujarlos.
 */
@Service
public class FacturaService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final PDFont FUENTE = PDType1Font.HELVETICA;
    private static final PDFont FUENTE_NEGRITA = PDType1Font.HELVETICA_BOLD;

    public byte[] generar(Venta venta) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            float margen = 50;
            float ancho = pagina.getMediaBox().getWidth() - 2 * margen;
            float y = pagina.getMediaBox().getHeight() - margen;

            try (PDPageContentStream cs = new PDPageContentStream(documento, pagina)) {
                // Encabezado
                y = escribir(cs, FUENTE_NEGRITA, 18, margen, y, "NGO SAECA");
                y = escribir(cs, FUENTE, 10, margen, y - 4, "Gestion de garantias y servicio tecnico");
                y -= 20;

                y = escribir(cs, FUENTE_NEGRITA, 14, margen, y, "Factura simple Nro. " + venta.getIdVenta());
                y = escribir(cs, FUENTE, 10, margen, y - 4,
                        "Fecha: " + (venta.getFecha() != null ? venta.getFecha().format(FORMATO_FECHA) : "-"));
                y = escribir(cs, FUENTE, 10, margen, y - 2,
                        "Vendedor: " + (venta.getVendedor() != null ? venta.getVendedor().getNombre() : "-"));

                String cliente = venta.getCliente() != null ? venta.getCliente().getNombre() : "Consumidor final";
                y = escribir(cs, FUENTE, 10, margen, y - 2, "Cliente: " + cliente);
                if (venta.getCliente() != null && tieneTexto(venta.getCliente().getDocumento())) {
                    y = escribir(cs, FUENTE, 10, margen, y - 2, "Documento: " + venta.getCliente().getDocumento());
                }

                y -= 20;

                // Tabla: encabezados
                float colArticulo = margen;
                float colCantidad = margen + 260;
                float colPrecio = margen + 330;
                float colSubtotal = margen + 430;

                trazarLinea(cs, margen, y + 4, margen + ancho, y + 4);

                escribirEn(cs, FUENTE_NEGRITA, 10, colArticulo, y - 12, "Articulo");
                escribirEn(cs, FUENTE_NEGRITA, 10, colCantidad, y - 12, "Cant.");
                escribirEn(cs, FUENTE_NEGRITA, 10, colPrecio, y - 12, "Precio unit.");
                escribirEn(cs, FUENTE_NEGRITA, 10, colSubtotal, y - 12, "Subtotal");
                y -= 20;

                trazarLinea(cs, margen, y + 4, margen + ancho, y + 4);
                y -= 10;

                for (DetalleVenta detalle : venta.getDetalles()) {
                    if (y < 80) {
                        // Una factura simple no debería tener tantos ítems como para no entrar
                        // en una página; si pasara, se corta acá en vez de romper el layout.
                        break;
                    }
                    String nombreArticulo = detalle.getArticulo() != null ? detalle.getArticulo().getNombre() : "-";
                    escribirEn(cs, FUENTE, 10, colArticulo, y, recortar(nombreArticulo, 38));
                    escribirEn(cs, FUENTE, 10, colCantidad, y, String.valueOf(detalle.getCantidad()));
                    escribirEn(cs, FUENTE, 10, colPrecio, y, formatoGs(detalle.getPrecioUnitario()));
                    escribirEn(cs, FUENTE, 10, colSubtotal, y, formatoGs(detalle.getSubtotal()));
                    y -= 16;
                }

                y -= 10;
                trazarLinea(cs, margen, y + 4, margen + ancho, y + 4);
                y -= 16;

                escribirEn(cs, FUENTE_NEGRITA, 12, colPrecio, y, "Total:");
                escribirEn(cs, FUENTE_NEGRITA, 12, colSubtotal, y, formatoGs(venta.getTotal()));

                y -= 40;
                escribir(cs, FUENTE, 8, margen, y,
                        "Documento generado por el sistema. No es un comprobante fiscal timbrado por la SET.");
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        }
    }

    // Escribe el texto y devuelve la posición Y para la siguiente línea
    private float escribir(PDPageContentStream cs, PDFont fuente, float tamano, float x, float y, String texto)
            throws IOException {
        escribirEn(cs, fuente, tamano, x, y, texto);
        return y - (tamano + 4);
    }

    // Escribe el texto en una posición fija, sin mover el cursor
    private void escribirEn(PDPageContentStream cs, PDFont fuente, float tamano, float x, float y, String texto)
            throws IOException {
        cs.beginText();
        cs.setFont(fuente, tamano);
        cs.newLineAtOffset(x, y);
        cs.showText(limpiar(texto));
        cs.endText();
    }

    private void trazarLinea(PDPageContentStream cs, float x1, float y1, float x2, float y2) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    // Formatea un monto en guaraníes con puntos como separador de miles (ej: "Gs. 2.750.000")
    private static String formatoGs(BigDecimal monto) {
        if (monto == null) monto = BigDecimal.ZERO;
        long valor = monto.setScale(0, RoundingMode.HALF_UP).longValueExact();
        String digitos = String.valueOf(Math.abs(valor));

        StringBuilder agrupado = new StringBuilder();
        int contador = 0;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            agrupado.append(digitos.charAt(i));
            contador++;
            if (contador % 3 == 0 && i != 0) agrupado.append('.');
        }

        return "Gs. " + (valor < 0 ? "-" : "") + agrupado.reverse();
    }

    private static String recortar(String texto, int maxCaracteres) {
        if (texto == null) return "";
        return texto.length() > maxCaracteres ? texto.substring(0, maxCaracteres - 3) + "..." : texto;
    }

    private static boolean tieneTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    // Las fuentes estándar (Helvetica) no soportan tildes/ñ con la codificación por
    // defecto: se quitan los diacríticos para que la factura no falle al generarse.
    private static String limpiar(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.replace('ñ', 'n').replace('Ñ', 'N');
    }
}
