import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from matplotlib import colors
import seaborn as sns


# set the text font size globally
plt.rcParams.update({'font.size': 12})

df_Ab = pd.read_csv("data/direx_Ab.csv")
df_Bb = pd.read_csv("data/direx_Bb.csv")
df_Aa = pd.read_csv("data/direx_Aa.csv")
df_Ba = pd.read_csv("data/direx_Ba.csv")

df_Aa = df_Aa[df_Aa["run"] != "run"]          # drop repeated header rows
df_Aa = df_Aa.apply(pd.to_numeric, errors="ignore")

df_Ba = df_Ba[df_Ba["run"] != "run"]          # drop repeated header rows
df_Ba = df_Ba.apply(pd.to_numeric, errors="ignore")

df_Bb = df_Bb[df_Bb["run"] != "run"]          # drop repeated header rows
df_Bb = df_Bb.apply(pd.to_numeric, errors="ignore")

df_Ab = df_Ab[df_Ab["run"] != "run"]          # drop repeated header rows
df_Ab = df_Ab.apply(pd.to_numeric, errors="ignore")

print("Data loaded:")
print("Ab:", df_Ab.shape)
print("Bb:", df_Bb.shape)
print("Aa:", df_Aa.shape)
print("Ba:", df_Ba.shape)

# Set seaborn style for better-looking plots
sns.set_style("whitegrid")
sns.set_palette("husl")

def plot_heatmap_subplot(ax, df, title):
    n1 = df["n1"].iloc[0]
    n2 = df["n2"].iloc[0]

    angles_deg = np.linspace(0, 180, 181)
    angles_rad = np.deg2rad(angles_deg)

    unique_angles = np.sort(df["angle2_deg"].unique())

    df["group_angle_deg"] = np.rad2deg(
        np.arctan2(df["dirY"], df["dirX"])
    )
    
    # Shift negative angles above 180: convert -180 to -90 → 180 to 270
    df["group_angle_deg"] = df["group_angle_deg"].apply(
        lambda x: x + 360 if x < -90 else x
    )

    x_bins = np.concatenate([
    [unique_angles[0] - 0.5],
    (unique_angles[:-1] + unique_angles[1:]) / 2,
    [unique_angles[-1] + 0.5]
    ])

    y_bins = np.linspace(-90, 270, 51)  # Extended range to accommodate shifted angles

    H, xedges, yedges = np.histogram2d(
        df["angle2_deg"],  # konfliktvinkel
        df["group_angle_deg"],
        bins=[x_bins, y_bins],
        density=False   # räkna antal; normalisera per x-bin nedan
    )

    g_dir_deg = []
    for theta in angles_rad:
        v1 = n1 * np.array([np.cos(0), np.sin(0)])       # n1 at 0°
        v2 = n2 * np.array([np.cos(theta), np.sin(theta)]) # n2 at theta
        g = v1 + v2
        angle = np.arctan2(g[1], g[0])  # radians
        angle_deg = np.rad2deg(angle)
        if angle_deg > 180:
            angle_deg -= 360
        # Apply same shift as data
        if angle_deg < -90:
            angle_deg += 360
        g_dir_deg.append(angle_deg)
    g_dir_deg = np.array(g_dir_deg)

    col_sums = H.sum(axis=1, keepdims=True) # sum over y-bins for each x-bin

    H_norm = np.divide(H, col_sums + 1e-12) # divide to get probabilities

    # Clip low values more aggressively and apply power transformation to emphasize peaks
    H_display = H_norm.copy()
    #H_display[H_display < 0.01] = 0  # More aggressive clipping
    #H_display = np.power(H_display, 0.4)  # Power transform to compress dynamic range

    im = ax.imshow(
        H_display.T,
        origin="lower",
        aspect="auto",
        extent=[
            xedges[0], xedges[-1],
            yedges[0], yedges[-1]
        ],
        # Sharper contrast: lower vmin and combine with LogNorm
        norm = colors.LogNorm(vmin=1e-2, vmax=H_display.max()),
        cmap="plasma"  # High-contrast colormap
        # ticks on colorbar handled outside
    )

    # Set background color to match the lowest colormap value
    cmap = plt.get_cmap("plasma")    
    bg_color = cmap(0.0)  # Color at the minimum (0.0) of the colormap
    ax.set_facecolor(bg_color)
    ax.plot(angles_deg, np.zeros_like(angles_deg), '--', color='white', linewidth=2, label='s1=0°')
    ax.plot(angles_deg, angles_deg, '--', color='white', linewidth=2, label='s2°')

    ax.plot(angles_deg, g_dir_deg, '-', color='white', linewidth=2, label='Mean preference direction')

    ax.set_xlim(xedges[0], xedges[-1])
    ax.set_ylim(-30, 210)
    
    # Set custom y-ticks with -90 shown as 270
    ax.set_yticks([0, 30, 60, 90, 120, 150, 180, 210])
    ax.set_yticklabels(['0', '30', '60', '90', '120', '150', '180', '-150'])

    #ax.set_title(f"{title} N=100, n1={n1}, n2={n2}", fontsize=18)
    # add legend style text box inside plot at upper left with n1 and n2
    ax.text(0.05, 0.95, f"$n_1$ = {n1}\n$n_2$ = {n2}", transform=ax.transAxes, fontsize=14,
            verticalalignment='top', bbox=dict(boxstyle='round', facecolor='white', alpha=0.8))
    ax.grid(False, color='gray', linestyle='--', alpha=0.5)
    
    return im

# Create 2x2 subplot figure
fig, axes = plt.subplots(2, 2, figsize=(12, 7), facecolor=(255/255, 255/255, 255/255))
#fig.suptitle('Feedback Direction Analysis: 2x2 Conditions', fontsize=14, fontweight='bold', y=0.995)

# Plot each heatmap
plot_heatmap_subplot(axes[0, 0], df_Ab, "No feedback")
plot_heatmap_subplot(axes[0, 1], df_Bb, "With feedback")
plot_heatmap_subplot(axes[1, 0], df_Aa, "No feedback")
im = plot_heatmap_subplot(axes[1, 1], df_Ba, "With feedback")


# Add a single colorbar for all subplots
cbar_ax = fig.add_axes([0.92, 0.15, 0.02, 0.7])
cbar = fig.colorbar(im, cax=cbar_ax)
# set colorbar ticks
cbar.set_ticks([1e-2, 1e-1, 35e-2])
cbar.set_ticklabels(['0.01', '0.1', '0.35'])
cbar.set_label('Probability of group direction ', fontsize=18)

# add title above first column to indicate no feedback
fig.text(0.28, 0.97, 'Without feedback', ha='center', fontsize=20)
# add title above second column to indicate feedback
fig.text(0.71, 0.97, 'With feedback', ha='center', fontsize=20)

# Add shared axis labels outside the plots
fig.text(0.5, 0.02, 'Preferred direction of groupset 2 (degrees)', ha='center', fontsize=18)
fig.text(0.02, 0.5, 'Group direction', va='center', rotation='vertical', fontsize=18)
# legend with line labels  
#fig.legend(loc='lower right', fontsize=9)


plt.tight_layout(rect=[0.05, 0.05, 0.9, 0.99])
plt.show()