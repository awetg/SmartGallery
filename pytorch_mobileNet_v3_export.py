import torch
import torchvision
from torch.utils.mobile_optimizer import optimize_for_mobile

 # use mobileNet v3 small model
model = torchvision.models.mobilenet_v3_small(pretrained=True)
model.eval()
traced_script_module = torch.jit.trace(model, torch.ones(1, 3, 224, 224))
optimized_traced_model = optimize_for_mobile(traced_script_module)
optimized_traced_model._save_for_lite_interpreter("app/src/main/assets/mobileNet_v3_small.pt")
