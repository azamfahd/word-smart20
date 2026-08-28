import subprocess
try:
    subprocess.check_output(['gradle', 'assembleDebug'], stderr=subprocess.STDOUT)
    print("Success")
except subprocess.CalledProcessError as e:
    print(e.output.decode('utf-8'))
