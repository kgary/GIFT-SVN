using System;
using System.Collections.Generic;

using System.Windows.Forms;


namespace ExampleTrainingApplication
{
    /// <summary>
    /// The 'Example Training Application' program is a simple program written in C# that is meant to be used as  
    /// an example training application with GIFT.  This application will present a dialog that the user can interact
    /// with as well as communicate via XML-RPC with the GIFT Gateway module's Example Plugin Interface (currently located at
    /// GIFT/src/mil/arl/gift/gateway/interop/example/ExamplePluginInterface.java in the GIFT folder structure).
    /// 
    /// Note: this application was written in C# to provide an example of how to integrate with GIFT in another 
    ///       programming languange (besides Java and C++ which has already been shown)
    /// 
    /// Note: XML-RPC was chosen as the communication protocol because it provides another example of how to communicate 
    ///       with GIFT (besides socket programming which has already been shown).  In addition XML-RPC is easier to extend
    ///       for beginner programmers because it utilizes method calling and lacks the need for message payload encode/decode protocols.
    /// </summary>
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new Form1());
        }
    }
}
