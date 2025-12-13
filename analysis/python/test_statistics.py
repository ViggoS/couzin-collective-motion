import pandas as pd
import numpy as np

#read a CSV file into a DataFrame

csv_file_path = 'a_vs_p_test.csv'
a_vs_p = pd.read_csv(csv_file_path)

#print("DataFrame loaded from CSV:")
#print(data.head())


# use circular statistics to calculate the accuracy of the direction for every configuration of N (number of agents) and n1 (number of informed agents) 
# and plot the results using matplotlib 
def calculate_group_accuracy(df):

    # preferred direction g
    target_angle_deg = df['angle1_deg'].iloc[0]
    g_rad = np.deg2rad(target_angle_deg)
    g_vec = np.array([np.cos(g_rad), np.sin(g_rad)])
    
    # normalize g_vec (should already be unit length but we ensure it)
    g_vec = g_vec / np.linalg.norm(g_vec)
    
    # extract h-vectors (group direction vectors)
    h_vecs = df[['dirX','dirY']].values
    
    # normalize each h_vec to avoid scaling issues
    norms = np.linalg.norm(h_vecs, axis=1, keepdims=True)
    h_normed = h_vecs / norms
    
    # dot product between each h and g
    dots = np.sum(h_normed * g_vec, axis=1)
    dots = np.clip(dots, -1.0, 1.0)  # numeric safety
    
    # angular deviation Δθ_i
    delta_theta = np.arccos(dots)   # radians
    
    # Couzin accuracy per row: 1 - Δθ/π
    accuracy = 1 - delta_theta / np.pi
    
    # aggregated accuracy (mean over replicates)
    mean_accuracy = accuracy.mean()
    
    print(f"Target angle (deg): {target_angle_deg}")
    print(f"Example accuracies: {accuracy[:5]}")
    print(f"Mean accuracy: {mean_accuracy:.4f}")


    
    return accuracy, mean_accuracy

#calculate_group_accuracy(a_vs_p)

import numpy as np
import pandas as pd


def compute_accuracy_per_row(df):
    # preferred direction g
    target_angle_deg = df['angle1_deg'].iloc[0]
    g_rad = np.deg2rad(target_angle_deg)
    g_vec = np.array([np.cos(g_rad), np.sin(g_rad)])
    g_vec = g_vec / np.linalg.norm(g_vec)

    # h vectors
    h_vecs = df[['dirX','dirY']].values
    h_norms = np.linalg.norm(h_vecs, axis=1, keepdims=True)
    h_normed = h_vecs / h_norms

    # dot products
    dots = np.sum(h_normed * g_vec, axis=1)
    dots = np.clip(dots, -1.0, 1.0)

    # angular deviation
    delta_theta = np.arccos(dots)

    delta_theta_min = np.min(delta_theta)
    delta_theta_max = np.max(delta_theta)

    # get p value of delta_theta_max
    p_value_max = df['p'].iloc[np.argmax(delta_theta)]
    print(f"P value at max delta theta: {p_value_max}")

    # accuracy per row
    accuracy = 1 - delta_theta / np.pi

    # normalize accuracy to [0, 1]
    accuracy = (accuracy + 1) / 2

    print(f"Delta theta min: {np.rad2deg(delta_theta_min):.2f} degrees")
    print(f"Delta theta max: {np.rad2deg(delta_theta_max):.2f} degrees")

    return accuracy

def analyze_accuracy(a_vs_p):
    a_vs_p['accuracy'] = compute_accuracy_per_row(a_vs_p)

    accuracy_summary = (
        a_vs_p
        .groupby(['N', 'p'])
        .agg(mean_accuracy=('accuracy', 'mean'),
            std_accuracy=('accuracy', 'std'),
            count=('accuracy', 'size'))
        .reset_index()
    )

    print("Accuracy summary:")
    print(accuracy_summary)

analyze_accuracy(a_vs_p)
