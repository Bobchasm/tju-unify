

def print_prompt(prompt, logger=None):
    if logger:
        logger.info(f"[print_prompt]" + "==========")
        logger.info(f"{prompt.to_string()}")
        logger.info(f"[print_prompt]" + "==========")
    else:
        print(f"[print_prompt]" + "==========")
        print(f"{prompt.to_string()}")
        print(f"[print_prompt]" + "==========")

    return prompt
