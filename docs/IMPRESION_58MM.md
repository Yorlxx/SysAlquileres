# Impresión de nota de venta en impresora térmica de 58 mm

## 1. ¿Qué se implementó?

En el módulo **Pagos** ahora aparece el botón **“Nota de venta”**. Al seleccionar un
pago de la tabla y presionar ese botón se abre una ventana con:

- La **vista previa** del ticket tal como saldrá impreso.
- Dos formas de conexión:
  1. **Windows (USB / Bluetooth):** usa la impresora instalada en Windows.
  2. **Puerto COM (Bluetooth):** envía los comandos directamente a la impresora.

> La nota de venta **no es un comprobante fiscal**. No reemplaza a la boleta ni a la
> factura electrónica de SUNAT; es un documento interno de control del alquiler.

---

## 2. ¿Cómo se usa?

1. Abrir el módulo **Pagos**.
2. Seleccionar un pago en la tabla.
3. Presionar **“Nota de venta”**.
4. Elegir la forma de conexión:
   - **Windows:** seleccionar la impresora (por ejemplo `POS-58-Series`).
   - **Puerto COM:** seleccionar el puerto (por ejemplo `COM5`) y la velocidad
     (normalmente `9600`).
5. Presionar **Imprimir**.

Si no se seleccionó ningún pago, el sistema muestra el mensaje
“Selecciona un pago para emitir la nota de venta.”

---

## 3. Archivos nuevos y modificados

| Archivo | Función |
|---|---|
| `impresion/DatosEmpresa.java` | Nombre, RUC, dirección y teléfono de la cabecera. |
| `impresion/NotaVenta.java` | Arma el texto del ticket en 32 caracteres por línea. |
| `impresion/ImpresoraWindows.java` | Imprime usando una impresora instalada en Windows. |
| `impresion/ImpresoraSerial.java` | Envía ESC/POS por un puerto COM (Bluetooth). |
| `components/NotaVentaDialog.java` | Ventana de vista previa y selección de impresora. |
| `controller/PagoController.java` | Método `onImprimirNota()` que abre la ventana. |
| `view/pagos.fxml` | Botón “Nota de venta”. |
| `pom.xml` | Dependencia `jSerialComm` para los puertos COM. |

---

## 4. ¿Por qué 32 caracteres por línea?

Una impresora térmica de 58 mm tiene **384 puntos** de ancho imprimible. La fuente
normal usa **12 puntos por carácter**:

```text
384 / 12 = 32 caracteres por línea
```

Por eso `NotaVenta.ANCHO = 32` y todas las líneas se centran, se recortan o se
alinean a la derecha pensando en ese ancho. Así el ticket no se desordena.

Ejemplo de una línea:

```text
TOTAL                  S/ 680.00
```

---

## 5. ¿Cómo funciona la impresión por cable (USB)?

```text
Sistema Java  →  driver de Windows  →  cable USB  →  impresora
```

1. El programa obtiene la lista de impresoras instaladas con
   `PrintServiceLookup.lookupPrintServices(...)`.
2. Se crea un `PrinterJob` de Java (AWT) y se le indica la impresora elegida.
3. Se define un papel de **58 mm de ancho** y un alto calculado según la cantidad
   de líneas del ticket.
4. Se dibuja cada línea con una fuente monoespaciada (`Font.MONOSPACED`), que
   hace que todas las letras ocupen lo mismo y las columnas queden alineadas.
5. `PrinterJob.print()` entrega el trabajo al **driver de Windows**, y el driver
   se encarga de hablar con la impresora por USB.

**Ventaja:** funciona con cualquier impresora que ya imprima desde Windows.
**Requisito:** la impresora debe estar instalada y encendida.

> En esta computadora el sistema detectó automáticamente `POS-58-Series` y
> `POS-80-Series`, así que se puede probar por USB seleccionando `POS-58-Series`.

---

## 6. ¿Cómo funciona la impresión por Bluetooth?

Windows no expone el Bluetooth como una impresora en todos los modelos. Lo que
hace la mayoría de impresoras térmicas económicas es crear un **puerto COM virtual**
al emparejarse (perfil **SPP**, Serial Port Profile).

```text
Sistema Java  →  puerto COM (Bluetooth)  →  impresora
```

1. Se empareja la impresora desde **Configuración → Bluetooth y dispositivos**.
2. Windows le asigna un puerto, por ejemplo `COM5`.
3. `ImpresoraSerial` abre ese puerto con **jSerialComm** a `9600` baudios
   (8 bits de datos, 1 bit de parada, sin paridad).
4. Se envían los bytes del ticket directamente, sin driver intermedio.

### Comandos ESC/POS que se usan

| Comando | Bytes | Significado |
|---|---|---|
| `ESC @` | `1B 40` | Inicializar la impresora |
| Texto | ASCII | Contenido del ticket |
| `ESC d 4` | `1B 64 04` | Avanzar 4 líneas al terminar |

A esto se le llama **ESC/POS**, el “idioma” estándar de las impresoras térmicas.

> Las tildes y la letra ñ se quitan antes de enviar por COM, porque muchas
> impresoras económicas no las interpretan bien. En la impresión por Windows sí
> se conservan.

---

## 7. ¿Por qué hay dos modos y no uno solo?

Porque no todas las impresoras se conectan igual:

| Conexión | Modo recomendado | Motivo |
|---|---|---|
| USB con driver instalado | Windows | El driver ya sabe hablar con la impresora. |
| Bluetooth emparejado como impresora | Windows | Windows la trata como impresora normal. |
| Bluetooth que crea puerto COM | Puerto COM | No hay driver de impresión; se envía ESC/POS directo. |
| USB que aparece como COM | Puerto COM | Se comporta como puerto serie. |

Así el sistema funciona en más modelos sin cambiar el código.

---

## 8. Cálculo del total

El ticket muestra:

```text
TOTAL = monto - descuento + IGV
```

Está implementado en `NotaVenta.total(Pago)` y usa `BigDecimal`, que es el tipo
adecuado para dinero porque no pierde centavos.

---

## 9. Personalización

Los datos de la cabecera están en:

```text
src/main/java/pe/edu/upeu/alquileres/impresion/DatosEmpresa.java
```

```java
public static final String NOMBRE = "INMOBILIARIA JULIACA";
public static final String RUC = "20123456789";
public static final String DIRECCION = "Av. Los Andes 123 - Juliaca";
public static final String TELEFONO = "Tel. 999 999 999";
```

Si se quiere cambiar el diseño del ticket (más líneas, otro texto, un logo en
texto), solo se modifica `NotaVenta.lineas(...)`.

---

## 10. Problemas frecuentes

| Problema | Causa probable | Solución |
|---|---|---|
| No aparece la impresora | No está instalada en Windows | Instalarla o emparejarla por Bluetooth. |
| “No se pudo abrir el puerto COM” | Puerto ocupado, apagado o mal emparejado | Encender, revisar el número de COM y desconectar otros programas. |
| Sale texto con símbolos raros | Codificación distinta | El modo COM ya envía ASCII sin tildes; revisar la página de códigos de la impresora. |
| Sale muy angosto o cortado | Ancho de papel distinto | Verificar que sea de 58 mm y usar `POS-58-Series`. |
| No corta el papel | La impresora no tiene cuchilla | Es normal en impresoras de 58 mm económicas. |
| Imprime una hoja larga | El driver ignora el alto personalizado | Ajustar el tamaño de papel en las propiedades de la impresora. |

---

## 11. Explicación corta para la exposición

> “Para la nota de venta usamos una impresora térmica de 58 mm. El sistema arma el
> ticket en líneas de 32 caracteres, que es lo que entra en ese ancho. Si la
> impresora está instalada en Windows, la mandamos por USB o Bluetooth a través del
> driver con `PrinterJob`. Si el Bluetooth solo crea un puerto COM, usamos
> `jSerialComm` y le enviamos comandos ESC/POS directamente. El total se calcula
> con `BigDecimal` y el documento es una nota interna, no un comprobante fiscal de
> SUNAT.”

---

## 12. Prueba recomendada antes de exponer

1. Encender la impresora y cargar papel de 58 mm.
2. Confirmar en Windows que aparece `POS-58-Series`.
3. Abrir el sistema → Pagos → seleccionar un pago → **Nota de venta**.
4. Probar primero el modo **Windows**.
5. Luego emparejar por Bluetooth, anotar el puerto COM y probar el modo
   **Puerto COM** con `9600` baudios.
6. Tener impreso un ticket de ejemplo por si falla el internet o el Bluetooth
   durante la exposición.
