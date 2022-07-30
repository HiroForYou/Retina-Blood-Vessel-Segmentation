import torch
import cv2
import numpy as np
from PIL import Image

from model import build_unet


CHECKPOINT_PATH = './weights/checkpoint.pth'
IMG_PATH = './img/image.png'

def mask_parse(mask):
    mask = np.expand_dims(mask, axis=-1)    ## (512, 512, 1)
    mask = np.concatenate([mask, mask, mask], axis=-1)  ## (512, 512, 3)
    return mask

model = build_unet()
model.load_state_dict(torch.load(CHECKPOINT_PATH, map_location='cpu'))
model.eval()

image = cv2.imread(IMG_PATH, cv2.IMREAD_COLOR) ## (512, 512, 3)
        ## image = cv2.resize(image, size)
x = np.transpose(image, (2, 0, 1))      ## (3, 512, 512)
x = x/255.0
x = np.expand_dims(x, axis=0)           ## (1, 3, 512, 512)
x = x.astype(np.float32)
x = torch.from_numpy(x)
pred_y = model(x)
print(pred_y)
pred_y = torch.sigmoid(pred_y)[0]
print(pred_y)
pred_y = np.squeeze(pred_y, axis=0)     ## (512, 512)
pred_y = pred_y > 0.5
pred_y = np.array(pred_y, dtype=np.uint8)
pred_y = mask_parse(pred_y)

img = Image.fromarray(pred_y * 255, 'RGB')
img.show()
