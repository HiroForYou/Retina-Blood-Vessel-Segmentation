# Segmentación de vasos sanguíneos de retina usando UNET (Modelo)

Este repositorio contiene el código para la segmentación semántica del vaso sanguíneo de la retina en el conjunto de datos DRIVE utilizando la arquitectura UNET en PyTorch.

# Arquitectura
El diagrama de bloques de la arquitectura UNET fue tomado del paper original.

| ![U-Net Architecture](img/u-net-architecture.png) |
| :--: |
| *Arquitectura U-Net* |

## Pesos de la red neuronal
Descargue los pesos de la red: [checkpoint.pth](https://drive.google.com/file/d/1Wl7-E6Tk3YpeJ7GIYScGvUeW9ou474yy/view?usp=sharing)

# Resultados
Las siguientes imágenes contienen:
1. Imagen de entrada
2. Máscara original
3. Máscara predicha

| ![](results/01_test_0.png) |
| :--: |
| ![](results/02_test_0.png) |
| ![](results/03_test_0.png) |
| ![](results/04_test_0.png) |