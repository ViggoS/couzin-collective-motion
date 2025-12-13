import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns   

# seaborn style for better aesthetics
# make background have color (247, 247, 247)
# make graph lines and dots varying shades of blue

df = pd.read_csv("experiment_1.csv")

sns.set(style="whitegrid", font_scale=2.5)
sns.set(rc={"axes.facecolor": (247/255, 247/255, 247/255),
            "figure.facecolor": (247/255, 247/255, 247/255)})


# 1. Preferred direction vector g
theta = np.deg2rad(df["angle1_deg"])
df["g_x"] = np.cos(theta)
df["g_y"] = np.sin(theta)

# 2. Normalize final direction h
norm = np.sqrt(df["dirX"]**2 + df["dirY"]**2)
df["h_x"] = df["dirX"] / norm
df["h_y"] = df["dirY"] / norm

# 3. cos(theta_k) och sin(theta_k)
df["cos_theta"] = df["h_x"] * df["g_x"] + df["h_y"] * df["g_y"]

# För sin(theta) behövs korsprodukten i 2D
df["sin_theta"] = df["h_x"] * df["g_y"] - df["h_y"] * df["g_x"]

# riktiga p
df["actual_p"] = df["n1"] / df["N"]

# 4. Gruppvis beräkning av accuracy = R
def compute_accuracy(group):
    Cbar = group["cos_theta"].mean()
    Sbar = group["sin_theta"].mean()
    R = np.sqrt(Cbar**2 + Sbar**2)
    return pd.Series({"accuracy": R})

accuracy_df = df.groupby(["N", "actual_p"]).apply(compute_accuracy).reset_index()

print(accuracy_df)

# plot accuracy vs p for different N values
# make hue be N

plt.figure(figsize=(5, 4))
sns.lineplot(data=accuracy_df, x='actual_p', y='accuracy',  
             hue='N', marker='o', palette='pastel')
plt.xlabel('Proportion of Informed Agents, p')
plt.ylabel('Accuracy')
plt.legend(title='Group Size', loc = 'lower right')
plt.tight_layout()
plt.show()


# 5. Gruppvis beräkning av elongation bbox_x/bbox_y
def compute_elongation(group):
    mean_bbox_x = group["bbox_X"].mean()
    mean_bbox_y = group["bbox_Y"].mean()
    elongation = mean_bbox_x / mean_bbox_y if mean_bbox_y != 0 else np.nan
    return pd.Series({"elongation": elongation})

# plot elongation vs p for different N values
elongation_df = df.groupby(["N", "actual_p"]).apply(compute_elongation).reset_index()

print(elongation_df)

plt.figure(figsize=(5, 4))
sns.lineplot(data=elongation_df, x='actual_p', y='elongation', 
             hue='N', marker='o', palette='pastel')
plt.xlabel('Proportion of Informed Agents, p')
plt.ylabel('Group elongation')
plt.legend(title='Group Size', loc = 'upper right')
plt.tight_layout()
plt.show()
