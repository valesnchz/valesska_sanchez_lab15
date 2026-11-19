# Guía Lab 15 – valesska_sanchez_lab15
Detección de malaria en glóbulos rojos: **Parasitized** vs **Uninfected** (dataset Malaria Cell Images, NIH).

## A. Colab
1. Sube `valesska_sanchez_lab15.ipynb` a Google Colab → Entorno: **GPU T4** → *Ejecutar todo* (unos 20-30 min).
2. Capturas para la diapositiva: celda **4** (`Test Accuracy` > 0.70, normalmente ~0.95) y celda **5** (inferencia con `np.argmax`).
3. La celda **5.1** prueba 4 imágenes (2 infectadas, 2 sanas) y las guarda en `test_images/`.
4. Descarga `entregables.zip`: trae `blood_cells_model.tflite`, `labels.txt` y `test_images/`.

## B. Android Studio
1. *New Project → Empty Views Activity* · Name: `valesska_sanchez_lab15` · Package: `com.example.valesska_sanchez_lab15` · Language: Kotlin · Build config: Kotlin DSL · Min SDK 24.
2. Copia y reemplaza con los archivos de esta carpeta:
   - `app/build.gradle.kts`
   - `app/src/main/java/com/example/valesska_sanchez_lab15/MainActivity.kt`
   - `app/src/main/res/layout/activity_main.xml`
3. Pega en `app/src/main/res/drawable/` las 4 imágenes de `test_images/` con estos nombres exactos (si falta alguna, no compila):
   `cell_parasitized_1.png`, `cell_parasitized_2.png`, `cell_uninfected_1.png`, `cell_uninfected_2.png`
4. Crea `app/src/main/assets/` (clic derecho en `main` → New → Folder → Assets Folder) y pega `blood_cells_model.tflite`.
5. *Sync Now* → corre en **Pixel 5 API 31**. Usa ◀ / ▶ para cambiar de imagen y **Label Image** para clasificar; captura cada una.

> Si tu proyecto usa `libs.versions.toml` (catálogo de versiones), deja el bloque `plugins { alias(...) }` que genera Android Studio y sólo añade `androidResources { noCompress += "tflite" }` y la dependencia `com.google.mlkit:image-labeling-custom:17.0.3`.

## C. Git / GitHub
Crea un repositorio vacío en github.com llamado `valesska_sanchez_lab15` (sin README) y en la carpeta del proyecto ejecuta:
```bash
git init
git add .
git commit -m "Lab15: detección de malaria en glóbulos rojos con ML Kit + TFLite"
git branch -M main
git remote add origin https://github.com/<TU_USUARIO>/valesska_sanchez_lab15.git
git push -u origin main
```
Sube también el notebook: cópialo en la raíz del proyecto antes del `git add`, o en Colab usa *Archivo → Guardar una copia en GitHub*.
(Android Studio ya genera un `.gitignore` que excluye `build/` y `.gradle/`.)

## D. Diapositiva única (.pptx), según la Fig. 1
Fila superior, de izquierda a derecha:
**[Imagen original del glóbulo rojo]  →  [icono/texto `blood_cells_model.tflite`]  →  [captura del emulador con la predicción]**

Fila inferior: **[captura de Colab: `Test Accuracy` > 0.70]** + **[captura de la inferencia individual con `np.argmax` → etiqueta]**

Texto breve:
- Título: *Detección de malaria en glóbulos rojos con MobileNetV2 + ML Kit* – Valesska Sánchez
- Dataset: Malaria Cell Images (NIH / National Library of Medicine): 27 558 glóbulos rojos, 2 clases
- Modelo: MobileNetV2 con Transfer Learning + fine-tuning, entrada 224×224, TFLite con metadatos
- Resultado: Test Accuracy = (tu valor)
- Enlace al repositorio de GitHub
