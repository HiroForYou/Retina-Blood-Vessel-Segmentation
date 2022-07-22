import torch
from torch.utils.mobile_optimizer import optimize_for_mobile
from model import build_unet


CHECKPOINT_PATH = './weights/checkpoint.pth'

model = build_unet()
model.load_state_dict(torch.load(CHECKPOINT_PATH, map_location='cpu'))
model.eval()

scripted_module = torch.jit.script(model)
optimized_scripted_module = optimize_for_mobile(scripted_module)

# Export full jit version model (not compatible with lite interpreter)
scripted_module.save("./weights/unet_scripted.pt")
# Export lite interpreter version model (compatible with lite interpreter)
scripted_module._save_for_lite_interpreter("./weights/unet_scripted.ptl")
# using optimized lite interpreter model makes inference about 60% faster than the non-optimized lite interpreter model, which is about 6% faster than the non-optimized full jit model
optimized_scripted_module._save_for_lite_interpreter("./weights/unet_scripted_optimized.ptl")

x = torch.randn((2, 3, 512, 512))
y = model(x)
print(y.shape)