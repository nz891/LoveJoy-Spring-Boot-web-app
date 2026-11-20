package com.love.joy.service;

import java.util.List;

public class RecaptchaAnswer
{
    // google returns success = true if captcha was solved correctly
    private boolean success;

    //if something went wrong then google sends error codes
    private List<String> errorCodes;

    public boolean isSuccess()
    {
        return success;
    }
    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public List<String> getErrorCodes()
    {
        return errorCodes;
    }
    public void setErrorCodes(List<String> errorCodes)
    {
        this.errorCodes = errorCodes;
    }
}
