import os
import json
from datetime import datetime
import matplotlib.pyplot as plt
import numpy as np


def calculate_execution_time(history):
    start_time = None
    stop_time = None

    for entry in history:
        if entry["queryState"] == "RUNNING":
            start_time = entry["timestampInUs"]
        elif entry["queryState"] == "STOPPED":
            stop_time = entry["timestampInUs"]

    if start_time is not None and stop_time is not None:
        execution_time_seconds = (stop_time - start_time) / 1e6  # Convert microseconds to seconds
        return execution_time_seconds
    else:
        return None

def process_json_file(file_path):
    file_name = os.path.basename(file_path)
    file_parts = file_name.split('-')

    query_id = file_parts[0]
    size = file_parts[1]
    topology = file_parts[-1].split('.')[0]
    
    with open(file_path, 'r') as file:
        data = json.load(file)

    execution_time = calculate_execution_time(data["history"])

    return query_id, topology, size, execution_time

def main(directory_path):
    results = []

    for file_name in os.listdir(directory_path):
        if file_name.endswith(".json"):
            file_path = os.path.join(directory_path, file_name)
            result = process_json_file(file_path)
            results.append(result)

    return results

    
def create_plots(data, output_directory=None):
    query_names = {
        'q1': 'DataPreProcessing',
        'q2': 'DataPreProcessingDistributed',
#        'q3': 'crowdEstimate',
        'q4': 'filterTimestamps',
#        'q5': 'kafkaRead',
#        'q6': 'KafkaReadStore',
        'q7': 'videoFramesPreProcessingWithMap',
        'q8': 'AgeProcessingWithMap'
    }

    topology_names = {
        't1': 'Cloud VM',
        't2': 'Edge'
    }
    
    size_mapping = {
        's1': '0.1',
        's2': '1',
        's3': '10',
        's4': '100'
        
    }

    for query_id in query_names.keys():
        query_data = [(item[1], item[2], item[3]) for item in data if item[0] == query_id]

        fig, ax = plt.subplots()

        bar_width = 0.25
        opacity = 0.8
        index = np.arange(len(set(item[1] for item in query_data)))

        for i, topology in enumerate(topology_names.keys()):
            execution_times_topology = sorted([item[2] for item in query_data if item[0] == topology])
            sizes = sorted(set(item[1] for item in query_data))
            
            plt.bar(index + i * bar_width, execution_times_topology, bar_width, alpha=opacity, label=f'Topology {topology_names[topology]}')

            # Print the bar plot data to the console
            print(f'Bar Plot Data for {query_names[query_id]} - Topology {topology_names[topology]}:')
            for j, size in enumerate(sizes):
                try:
                    time = execution_times_topology[j]
                    print(f'Size {size_mapping[size]}: {time} seconds')
                except IndexError:
                    pass
                
        plt.xlabel('Input Size (MB)')
        plt.ylabel('Execution Time (seconds)')
        plt.title(f'{query_names[query_id]} Execution Times')
        plt.xticks(index + bar_width * (len(topology_names) - 1) / 2, [size_mapping[size] for size in sizes])
        plt.legend()
        plt.grid(True)

        # Check if the output directory exists, create it if not
        if output_directory and not os.path.exists(output_directory):
            os.makedirs(output_directory)

        # Save the plot as a PNG file in the specified output directory
        if output_directory:
            output_path = os.path.join(output_directory, f'{query_id}_execution_times.png')
            plt.savefig(output_path, dpi=300)
        else:
            plt.savefig(f'{query_id}_execution_times.png', dpi=300)

        # Show the plot (optional)
        plt.show()


def extract_query_plan(json_file_path):
    # Load the JSON data from file
    with open(json_file_path, 'r') as file:
        data = json.load(file)

    # Extract the queryPlan content
    query_plan_text = data.get("queryPlan", "")

    return query_plan_text

def create_query_plan_figure(query_plan_text, output_path):
    # Create a text plot for the queryPlan
    fig, ax = plt.subplots(figsize=(8, 6))
    ax.text(0.1, 0.5, query_plan_text, fontsize=10, va='center', ha='left', fontfamily='monospace')

    # Hide axes
    ax.axis('off')

    # Save the plot as a PNG file with higher DPI (e.g., 300)
    plt.savefig(output_path, dpi=300, bbox_inches='tight', pad_inches=0.1)

    # Close the figure to avoid memory issues when processing multiple files
    plt.close(fig)

def process_directory(directory_path, output_directory):
    # Create the output directory if it doesn't exist
    os.makedirs(output_directory, exist_ok=True)

    # Iterate through all files in the directory
    for filename in os.listdir(directory_path):
        if filename.endswith(".json"):
            json_file_path = os.path.join(directory_path, filename)
            query_plan_text = extract_query_plan(json_file_path)

            # Create a unique output file path for each JSON file
            output_path = os.path.join(output_directory, f'{os.path.splitext(filename)[0]}_query_plan_figure.png')

            create_query_plan_figure(query_plan_text, output_path)

if __name__ == "__main__":
    directory_path = "/home/vmatsoukas/Documents/Ubitech_Projects/video-surveillance-ubi-uc/video-meta-analytics/query_measurements"
    data = main(directory_path)
    create_plots(data, "/home/vmatsoukas/Documents/Ubitech_Projects/video-surveillance-ubi-uc/video-meta-analytics/plots_execution_times")
    input_directory_path = directory_path
    output_directory_path = "/home/vmatsoukas/Documents/Ubitech_Projects/video-surveillance-ubi-uc/video-meta-analytics/query_plan_figures"
    process_directory(input_directory_path, output_directory_path)
