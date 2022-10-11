@echo off
setlocal


:main
	call :initSettings
	call :saveSettings
	
	call :setupConda
	call :runStableDiffusion
	call :shutdownConda
	
    exit


:initSettings
	:directorySettings
		set base_drive=C:
		set base_dir=%UserProfile%\stable-diffusion
		set conda_dir=C:\python\Miniconda3
		set model_dir=F:\Datasets\Neural Networks\Stable Diffusion
		set output_dir=%base_dir%\output
	
	:projectSettings
		set conda_env=ldm
		set branch_name=stable-diffusion-main
		set model_file=%model_dir%\sd-v1-4.ckpt
		set config_file=
		set use_optimized_script=true
	
	:promptSettings
		set prompt_file=%base_dir%\prompt.txt
		if exist "%prompt_file%" set /p prompt_file_data=<"%prompt_file%"
		set prompt_text=
		if "%prompt_text%"=="" (set pass_prompt_as_file=true) else (set pass_prompt_as_file=false)
		set guidance_scale=7.5
	
	:outputSettings
		set num_iterations=5
		set image_width=512
		set image_height=512
		set image_format=png
		set skip_grid=true
		set skip_save=false
	
	:processSettings
		set custom_seed=
		set fixed_code=false
		set sampling_method=plms
		set latent_channels=4
		set ddim_steps=50
		set ddim_eta=0.0
		set downsampling_factor=8
		set batch_size=1
		set cast_precision=autocast
		set use_laion400m=false
		set PYTORCH_CUDA_ALLOC_CONF=max_split_size_mb:128
	
	exit /b 0


:runStableDiffusion
	:chooseScript
		if %use_optimized_script%==true (
			set script_file=optimizedSD\optimized_txt2img.py
		) else (
			set script_file=scripts\txt2img.py
		)
	
	:setArguments
		set script_args=
		call :addScriptArg --prompt "%prompt_text%"
		call :addScriptArg --outdir "%output_dir%"
		call :addScriptArg --skip_grid ~ %skip_grid%
		call :addScriptArg --skip_save ~ %skip_save%
		call :addScriptArg --ddim_steps %ddim_steps%
		call :addScriptArg --sampler %sampling_method% %use_optimized_script% --%sampling_method%
		call :addScriptArg --laion400m ~ %use_laion400m%
		call :addScriptArg --fixed_code ~ %fixed_code%
		call :addScriptArg --ddim_eta %ddim_eta%
		call :addScriptArg --n_iter %num_iterations%
		call :addScriptArg --H %image_height%
		call :addScriptArg --W %image_width%
		call :addScriptArg --C %latent_channels%
		call :addScriptArg --f %downsampling_factor%
		call :addScriptArg --n_samples %batch_size%
		call :addScriptArg --scale %guidance_scale%
		call :addScriptArg --from-file "%prompt_file%" %pass_prompt_as_file%
		call :addScriptArg --format %image_format% %use_optimized_script%
		call :addScriptArg --config "%config_file%"
		call :addScriptArg --ckpt "%model_file%"
		call :addScriptArg --seed %custom_seed%
		call :addScriptArg --precision %cast_precision%
	
	:runScript
		python %script_file% %script_args%
	
	exit /b 0


:setupConda
    %base_drive% && cd "%base_dir%\%branch_name%"
	call %conda_dir%\Scripts\activate.bat %conda_env%
	exit /b 0


:shutdownConda
	call conda deactivate
	cd "%base_dir%"
	exit /b 0


:saveSettings
	:setTimestamp
		set date_key=%DATE:~-4%%DATE:~4,2%%DATE:~7,2%
		set time_key=%TIME:~-11,2%%TIME:~-8,2%%TIME:~-5,2%
		set timestamp=%date_key% %time_key: =0%
	
	:writeSettings
		set settings_file=%output_dir%\%timestamp%.txt
		(
			echo Prompt:       %prompt_text%%prompt_file_data%
			echo Scale:        %guidance_scale%
			echo Iterations:   %num_iterations%
			echo Width:        %image_width%
			echo Height:       %image_height%
			echo Format:       %image_format%
			echo Skip Grid:    %skip_grid%
			echo Skip Save:    %skip_save%
			echo Custom Seed:  %custom_seed%
			echo Fixed Code:   %fixed_code%
			echo Sampler:      %sampling_method%
			echo Channels:     %latent_channels%
			echo DDIM Steps:   %ddim_steps%
			echo DDIM ETA:     %ddim_eta%
			echo Downsampling: %downsampling_factor%
			echo Batch Size:   %batch_size%
			echo Precision:    %cast_precision%
			echo LAION400M:    %use_laion400m%
			echo Prompt File:  %prompt_file%
			echo Model File:   %model_file%
			echo Config File:  %config_file%
			echo Optimized:    %use_optimized_script%
		)>"%settings_file%"
	
	exit /b 0


:addScriptArg
	if "%~2"=="" (
		set script_args=%script_args%
	) else if "%~3"=="" (
		set script_args=%script_args% %1 %2
	) else if "%~3"=="true" (
		if "%~2"=="~" (
			set script_args=%script_args% %1
		) else (
			set script_args=%script_args% %1 %2
		)
	) else if "%~4"=="" (
		set script_args=%script_args%
	) else (
		if "%~5"=="" (
			set script_args=%script_args% %4
		) else (
			set script_args=%script_args% %4 %5
		)
	)
	exit /b 0
