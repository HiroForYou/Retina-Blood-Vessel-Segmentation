import torch
from torch.utils.mobile_optimizer import optimize_for_mobile


torch.hub.set_dir("./weights")
model = torch.hub.load('mateuszbuda/brain-segmentation-pytorch', 'unet',
    in_channels=3, out_channels=1, init_features=32, pretrained=True)
model.eval()

scripted_module = torch.jit.script(model)
optimized_scripted_module = optimize_for_mobile(scripted_module)

# Export full jit version model (not compatible with lite interpreter)
scripted_module.save("./weights/unet_brain_scripted.pt")
# Export lite interpreter version model (compatible with lite interpreter)
scripted_module._save_for_lite_interpreter("./weights/unet_brain_scripted.ptl")
# using optimized lite interpreter model makes inference about 60% faster than the non-optimized lite interpreter model, which is about 6% faster than the non-optimized full jit model
optimized_scripted_module._save_for_lite_interpreter("./weights/unet_brain_scripted_optimized.ptl")

x = torch.randn((2, 3, 512, 512))
y = model(x)
print(y.shape)