## This script is used to claim and run jobs from the operator. Currently, after the job is finished and the result is sent to the operator, the container will be stopped.
import requests
import json
import time
import os
import shutil
from colorama import Fore, Style

# get operator_base_url and runner_key from environment variables
operator_base_url = os.getenv('OPERATOR_BASE_URL')
runner_key = os.getenv('RUNNER_KEY')
runner_id = os.getenv('RUNNER_ID')

# check if operator_base_url and runner_key are set
if operator_base_url is None or runner_key is None or runner_id is None:
    print('Please set OPERATOR_BASE_URL and RUNNER_KEY and RUNNER_ID environment variables')
    exit(1)
if operator_base_url[-1] == '/':
    operator_base_url = operator_base_url[:-1]

def set_job_status(job_id, status):
    response = requests.post(f'{operator_base_url}/jobs/update?id={job_id}&key={runner_key}', json={"status": status})
    # check if response is valid
    if response.status_code != 200:
        print(f'Error updating job status: {response.text}')
        return
    print(f'Updated job {job_id} status to {status}')
    return response.json()

def send_results(job_id, grade, rubric):
    response = requests.post(f'{operator_base_url}/jobs/update?id={job_id}&key={runner_key}', json={"status": "completed", "grade": grade, "rubric": rubric})
    # check if response is valid
    if response.status_code != 200:
        print(f'Error sending results: {response.text}')
        return
    print(f'Sent results for job {job_id}')
    return response.json()

def set_runner_status(status,job_id=None):
    query_str = f'{operator_base_url}/runners/update?id={runner_id}&status={status}&key={runner_key}'
    if(job_id is not None):
        query_str += f'&current_job_id={job_id}'
    response = requests.post(query_str)
    # check if response is valid
    if response.status_code != 200:
        print(f'Error updating runner status: {response.text}')
        return
    print(f'Updated runner {runner_id} status to {status}')
    return response.json()

# get all jobs from operator
def get_jobs():
    print('Fetching available jobs...')
    response = requests.get(f'{operator_base_url}/jobs?pending_only=true&key={runner_key}')
    # check if response is valid
    if response.status_code != 200:
        print(f'Error getting jobs: {response.text}')
        return {}
    response_json = response.json()
    if 'jobs' not in response.json():
        print('Invalid response from operator, "jobs" key not found')
        return {}
    jobs = response_json['jobs']
    print(f'Found {len(jobs)} total jobs')
    return jobs

# claim a job
def claim_job(job):
    job_id = job['id']
    job_name = job['name']
    print(f'Claiming job {job_id} ({job_name})')
    response = requests.post(f'{operator_base_url}/jobs/claim?id={job_id}&key={runner_key}')
    # check if response is valid
    if response.status_code != 200:
        print(f'Error claiming job: {response.text}')
        return
    if response.json()['id'] != job_id:
        print(f'Error claiming job: job id mismatch')
        return
    print(f'Successfully claimed job {job_id} ({job_name})')
    return response.json()

def prepare_job(job):
    # clean up the current directory
    print('Cleaning up current directory...')
    for file in os.listdir('.'):
        if file != 'lab-runner.py':
            if os.path.isdir(file):
                shutil.rmtree(file)
            else:
                os.remove(file)
    # validate that job has tests_file_url and submission_file_url
    if 'tests_file_url' not in job or 'submission_file_url' not in job:
        print('Job does not have tests_file_url or submission_file_url')
        return
    # download tests file and save it as tests_file.zip
    tests_file_url = job['tests_file_url']
    response = requests.get(tests_file_url)
    with open('tests_file.zip', 'wb') as file:
        file.write(response.content)
    print(f'Downloaded tests file to tests_file.zip')
    # unzip tests_file.zip to cwd
    shutil.unpack_archive('tests_file.zip', '.')
    print(f'Unzipped tests file to current directory')
    # download submission file and save it in the submissions directory, but keep the original file name. The file name may not be part of the submission file URL.
    submission_file_url = job['submission_file_url']
    response = requests.get(submission_file_url)
    submission_file_name = response.headers['Content-Disposition'].split('filename=')[1][1:-1]
    # ensure that the submissions directory exists
    if not os.path.exists('submissions'):
        os.makedirs('submissions')
    # save the submission file in the submissions directory
    with open(f'submissions/{submission_file_name}', 'wb') as file:
        file.write(response.content)
    print(f'Downloaded submission file to submissions/{submission_file_name}')

def run_job(job):
    # run the job
    print('Running the job...')
    # set the job status to running
    job_id = job['id']
    set_job_status(job_id, 'running')
    set_runner_status('running',job_id)
    print(f'Updated job {job_id} status to running')
    # run the tests
    os.system('jagr --no-export')
    # normalize terminal output (color codes, etc.)
    print(Style.RESET_ALL)

    print('Grading completed, verifying results...')
    # find the results file
    rubrics_file_name = None
    for file in os.listdir('rubrics/lab'):
        if file.endswith('.json'):
            rubrics_file_name = file
            break
    if rubrics_file_name is None:
        print('Results file not found')
        set_runner_status('failed')
        set_job_status(job_id, 'failed')
        exit(1)
    # read the results from the rubrics/lab/*.json file
    results = None
    with open(f'rubrics/lab/{rubrics_file_name}', 'r') as file:
        results = json.load(file)
    # check for totalPointsMin and totalPointsMax in the results
    if 'totalPointsMin' not in results or 'totalPointsMax' not in results:
        print('totalPointsMin or totalPointsMax not found in results')
        set_runner_status('failed')
        set_job_status(job_id, 'failed')
        exit(1)
    total_points_min = results['totalPointsMin']
    total_points_max = results['totalPointsMax']
    # update the job status to completed
    set_job_status(job_id, 'completed')
    set_runner_status('success')
    print(f'Updated job {job_id} status to completed')
    print(f'Job {job_id} completed with {total_points_min} to {total_points_max} points')
    # send the results to the operator
    send_results(job_id, total_points_min, results)
    return results

def main():
    set_runner_status('running')
    jobs=get_jobs()
    if len(jobs) == 0:
        print('No jobs available')
        return

    # find jobs that have "base_image" set to "jagr-lab-runner:latest"
    legible_jobs = [job for job in jobs if job['base_image'] == 'jagr-lab-runner:latest']
    if len(legible_jobs) == 0:
        print('No legible jobs available')
        return
    print(f'Found {len(legible_jobs)} legible jobs')

    # claim the first job
    job=claim_job(legible_jobs[0])

    # prepare the job
    prepare_job(job)

    # run the job
    run_job(job)

if __name__ == '__main__':
    main()
