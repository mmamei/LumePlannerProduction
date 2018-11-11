package services;

import java.util.Properties;

public class Config
{
    Properties configFile;

    public Config()
    {
        this.configFile = new Properties();
        try
        {
            this.configFile.load(getClass().getClassLoader()
                    .getResourceAsStream("config.cfg"));
        }
        catch (Exception eta)
        {
            eta.printStackTrace();
        }
    }

    public String getProperty(String key)
    {
        String value = this.configFile.getProperty(key);
        return value;
    }
}
