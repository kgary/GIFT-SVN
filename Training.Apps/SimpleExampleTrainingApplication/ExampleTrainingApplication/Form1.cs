using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;

using System.Text;
using System.Windows.Forms;

using System.Threading;
using System.Net.Sockets;
using System.Net;

using System.IO;
using CookComputing.XmlRpc;
using System.Collections;

using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Http;
using System.Runtime.Remoting;

namespace ExampleTrainingApplication
{

    /// <summary>
    /// This class is responsible for building the dialog that will be presented to the user.
    /// In addition it is responsible for communicating with the GIFT interop plugin of
    /// mil.arl.gift.gateway.interop.simple.SimpleExampleTAPluginInterface.
    /// </summary>
    public partial class Form1 : Form
    {
        //thread stuff
        private bool isRunning = true;
        private Thread serverThread;

        //XML RPC stuff
        private int gift_xml_rpc_server_port;
        private int my_xml_rpc_server_port;
        private IPAddress gift_xml_rpc_server_ip_address;
        HttpListener listener = null;

        //
        // Property file stuff
        //
        private static String BIN_PATH = System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().GetName().CodeBase).Replace("file:\\", "");
        private static String PROPERTY_FILENAME = BIN_PATH + "\\..\\..\\..\\application.properties";
        private static String COMMENT_LINE = "#";
        private static char PROPERTY_DELIM = '=';
        
        // Property Keys
        private static String GIFT_XML_RPC_SERVER_PORT_KEY          = "GIFT_XML_RPC_SERVER_PORT";
        private static String MY_XML_RPC_SERVER_PORT_KEY            = "MY_XML_RPC_SERVER_PORT";
        private static String GIFT_XML_RPC_SERVER_IP_ADDRESS_KEY    = "GIFT_XML_RPC_SERVER_IP_ADDRESS";
        private static String USING_GIFT_XML_RPC_SERVER_TEST_KEY    = "USING_GIFT_XML_RPC_SERVER_TEST";

        // flag to indicate whether or not the GIFT XML-RPC Server class's main method is running in order to use it's server hosted
        // inner class of Example.class.  The Example class contains test methods this application can use.
        private bool using_GIFT_XML_RPC_Server_Test = false;

        private Dictionary<string, string> properties = new Dictionary<string,string>();

        delegate void SetTextCallback(string text);

        /// <summary>
        /// Class constructor - build dialog, read properties and create XML-RPC server instance
        /// </summary>
        public Form1()
        {
            try
            {
                //read the properties file
                readProperties();

                //setup connection
                setup();

                
            }
            catch (Exception e)
            {
                MessageBox.Show("Caught exception while starting application:\n" + e, "ERROR", MessageBoxButtons.OK);
                Environment.Exit(1);
            }

            InitializeComponent();

            disableButtons();
        }

        public void disableButtons()
        {
            this.button1.Enabled = false;
            this.button2.Enabled = false;
            this.button3.Enabled = false;
        }

        public void enableButtons()
        {
            this.button1.Enabled = true;
            this.button2.Enabled = true;
            this.button3.Enabled = true;
        }

        /// <summary>
        /// Called when the form is closing in order to cleanup this class
        /// </summary>
        /// <param name="e"></param>
        protected override void OnClosing(CancelEventArgs e)
        {
            //stop any running threads/connections
            stop();
        }

        /// <summary>
        /// Read the properties file and store the properties
        /// </summary>
        private void readProperties()
        {
            //MessageBox.Show("Using property file name of "+PROPERTY_FILENAME+".", "Debugging", MessageBoxButtons.OK);

            foreach (var row in File.ReadAllLines(PROPERTY_FILENAME))
            {

                if (row.StartsWith(COMMENT_LINE) || row.Trim().Length == 0)
                {
                    //found comment or empty line
                    continue;
                }
                else if (!row.Contains(PROPERTY_DELIM.ToString()))
                {
                    //found badly formatted line
                    continue;
                }

                int firstDelimIndex = row.IndexOf(PROPERTY_DELIM);
                String name = row.Substring(0, firstDelimIndex);
                String value = row.Substring(firstDelimIndex+1);
                properties.Add(name, value);
            }

            //
            // Set local variables based on property values
            //
            if (!properties.ContainsKey(GIFT_XML_RPC_SERVER_IP_ADDRESS_KEY))
            {
                //missing GIFT Server IP address property
                MessageBox.Show("Unable to find a property value for " + GIFT_XML_RPC_SERVER_IP_ADDRESS_KEY + " in the application.property file", "ERROR", MessageBoxButtons.OK);
                Environment.Exit(1);
            }
            else
            {
                gift_xml_rpc_server_ip_address = IPAddress.Parse(properties[GIFT_XML_RPC_SERVER_IP_ADDRESS_KEY]);
            }

            if (!properties.ContainsKey(GIFT_XML_RPC_SERVER_PORT_KEY))
            {
                //missing GIFT Server port property
                MessageBox.Show("Unable to find a property value for " + GIFT_XML_RPC_SERVER_PORT_KEY + " in the application.property file", "ERROR", MessageBoxButtons.OK);
                Environment.Exit(1);
            }
            else
            {
                gift_xml_rpc_server_port = int.Parse(properties[GIFT_XML_RPC_SERVER_PORT_KEY]);
            }

            if (!properties.ContainsKey(MY_XML_RPC_SERVER_PORT_KEY))
            {
                //missing application's server port property
                MessageBox.Show("Unable to find a property value for " + MY_XML_RPC_SERVER_PORT_KEY + " in the application.property file", "ERROR", MessageBoxButtons.OK);
                Environment.Exit(1);
            }
            else
            {
                my_xml_rpc_server_port = int.Parse(properties[MY_XML_RPC_SERVER_PORT_KEY]);
            }

            //optional server test property
            if (properties.ContainsKey(USING_GIFT_XML_RPC_SERVER_TEST_KEY))
            {
                String value = properties[USING_GIFT_XML_RPC_SERVER_TEST_KEY];
                if (value != null && value.Equals("true"))
                {
                    using_GIFT_XML_RPC_Server_Test = true;
                }
            }

        }

        /// <summary>
        /// Setup this class and any threads/connections needed
        /// </summary>
        private void setup()
        {            
            if (serverThread == null || !serverThread.IsAlive) { serverThread = new Thread(startThread); }

            if (!serverThread.IsAlive)
            {
                serverThread.Start();
            }
        }

        /// <summary>
        /// Stop any threads/connections created by this class
        /// </summary>
        public void stop()
        {
            isRunning = false;

            if (listener != null)
            {
                listener.Stop();
            }
        }

        /// <summary>
        /// The 'button1' was clicked.  Notify GIFT.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void button1_Click(object sender, EventArgs e)
        {
            sendStateMessage("button 1");
        }

        /// <summary>
        /// The 'button2' was clicked.  Notify GIFT.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void button2_Click(object sender, EventArgs e)
        {
            sendStateMessage("button 2");
        }

        /// <summary>
        /// The 'button3' was clicked.  Notify GIFT.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void button3_Click(object sender, EventArgs e)
        {
            //sendStateMessage("button 3");
            disableButtons();

            sendFinishedState();
        }

        /// <summary>
        /// Add a string to the receive list box on the dialog
        /// </summary>
        /// <param name="data"></param>
        public void updateReceiveListBox(String data)
        {
            if (receivedListBox.InvokeRequired)
            {
                SetTextCallback d = new SetTextCallback(updateReceiveListBox);
                this.Invoke(d, new Object[] { data });
            }
            else
            {
                receivedListBox.Items.Add(data);
            }
        }

        /// <summary>
        /// Add a string to the sent list box on the dialog
        /// </summary>
        /// <param name="data"></param>
        public void updateSentListBox(String data)
        {
            if (sentListBox.InvokeRequired)
            {
                SetTextCallback d = new SetTextCallback(updateSentListBox);
                this.Invoke(d, new Object[] { data });
            }
            else
            {
                sentListBox.Items.Add(data);
            }
            
        }

        /// <summary>
        /// Returns the IPv4 address of the machine running this application
        /// </summary>
        /// <returns>the IPv4 address</returns>
        private String getIPAddress()
        {
            IPHostEntry host;
            string localIP = null;
            host = Dns.GetHostEntry(Dns.GetHostName());
            foreach (IPAddress ip in host.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    localIP = ip.ToString();
                }
            }

            if (localIP == null)
            {
                throw new Exception("Unable to get the IP address");
            }

            return localIP;
        }
        
        /// <summary>
        /// Start the socket that receives packets from iLink App
        /// </summary>
        private void startThread()
        {            
            try
            {
                //Create the XML-RPC Server connection
                listener = new HttpListener();

                //Note: if you use any other address besides 'localhost' you will need admin privelages to 'start' the listener (below).
                //      I don't like this because when your running a GIFT course a prompt will appear for each lesson that launches this app.
                String addressToUse = "localhost";
                listener.Prefixes.Add("http://" + addressToUse + ":" + my_xml_rpc_server_port + "/");
                listener.Start();

                //listen for requests until told to stop
                while (isRunning)
                {
                    HttpListenerContext context = listener.GetContext();
                    ListenerService svc = new StateNameService(this);
                    svc.ProcessRequest(context);
                }

            }
            catch (Exception e)
            {
                if (isRunning)
                {
                    MessageBox.Show("Caught exception while running listener:\n" + e, "ERROR", MessageBoxButtons.OK);
                    Environment.Exit(1);
                }
            }
            finally
            {
                //cleanup
                if (listener != null && listener.IsListening)
                {
                    try
                    {
                        listener.Stop();
                        listener = null;
                    }
                    catch (Exception e) 
                    {
                        MessageBox.Show("Caught exception while running listener:\n" + e, "ERROR", MessageBoxButtons.OK);
                    }
                }
            }

            //MessageBox.Show("Good bye thread");

        }

        /// <summary>
        /// Notify GIFT of a 'finished' condition for this training application.
        /// This is useful for testing a GIFT courses ability to end a lesson based on DKF end trigger rules.
        /// </summary>
        private void sendFinishedState()
        {
            ISumAndDiff proxy = (ISumAndDiff)XmlRpcProxyGen.Create(typeof(ISumAndDiff));
            proxy.Url = "http://" + gift_xml_rpc_server_ip_address + ":" + gift_xml_rpc_server_port + "/";

            if (!using_GIFT_XML_RPC_Server_Test && proxy != null)
            {
                try
                {
                    //this is an RPC method, currently, hosted by mil.arl.gift.gateway.interop.simple.SimpleExampleTAPluginInterface$SimpleExampleTAPluginXMLRPC.class
                    proxy.trainingApplicationFinished();
                }
                catch (Exception e)
                {
                    MessageBox.Show("Caught exception while trying to notify GIFT of an Application Finished state:\n" + e, "ERROR", MessageBoxButtons.OK);
                }
            }

            updateSentListBox("finished notification");
        }

        /// <summary>
        /// Send data to GIFT via XML-RPC connection
        /// </summary>
        /// <param name="data">Data to send</param>
        private void sendStateMessage(string data)
        {
            ISumAndDiff proxy = (ISumAndDiff)XmlRpcProxyGen.Create(typeof(ISumAndDiff));
            proxy.Url = "http://" + gift_xml_rpc_server_ip_address + ":" + gift_xml_rpc_server_port + "/";

            if (using_GIFT_XML_RPC_Server_Test)
            {
                //if your using the mil.arl.gift.net.xmlrpc.XMLRPCServer.class main method then set the property in 
                //the properties file to "true"
                String response = proxy.test(data);
                updateReceiveListBox(response);

            }
            else
            {
                //this is an RPC method, currently, hosted by mil.arl.gift.gateway.interop.simple.SimpleExampleTAPluginInterface$SimpleExampleTAPluginXMLRPC.class
                proxy.trainingApplicationStateMessage(data);
            }

            updateSentListBox(data);
        }

        private void clearReceivedListButton_Click(object sender, EventArgs e)
        {
            receivedListBox.Items.Clear();
        }

        private void clearSendListButton_Click(object sender, EventArgs e)
        {
            sentListBox.Items.Clear();
        }

    }//end Form1 class

    /**************************************XML-RPC Server stuff ************************************************************************************/

    /// <summary>
    /// The derived class which contains the implementation of the methods required in the XML-RPC service supported
    /// by this applications XML-RPC server.
    /// </summary>
    public class StateNameService : ListenerService
    {
        // Used to update the form's received message listbox
        private Form1 form;

        public StateNameService(Form1 form)
        {
            this.form = form;
        }

        [XmlRpcMethod("load",
                Description = "GIFT is providing a SIMAN load message with the course's load parameters.")]
        public String load(String scenarioName)
        {
            //do something, in this case show some text on the dialog
            form.updateReceiveListBox("load w/ scenario name of " + scenarioName);

            form.enableButtons();

            return "success";
        }

        [XmlRpcMethod("blob",
        Description = "GIFT is providing a generic message contain a string.")]
        public void blob(String text)
        {
            //do something, in this case show some text on the dialog
            form.updateReceiveListBox(text);
        }

        [XmlRpcMethod("closeApplication",
        Description = "GIFT is ordering this application to close (i.e. terminate this program).")]
        public void closeApplication()
        {
            Thread closeThread = new Thread(closeForm);
            closeThread.Start();
        }

        private void closeForm()
        {
            form.Close();
        }
    }

    /// <summary>
    /// Handles XML-RPC requests
    /// </summary>
    public abstract class ListenerService : XmlRpcHttpServerProtocol
    {
        public virtual void ProcessRequest(HttpListenerContext RequestContext)
        {
            try
            {
                IHttpRequest req = new ListenerRequest(RequestContext.Request);
                IHttpResponse resp = new ListenerResponse(RequestContext.Response);
                HandleHttpRequest(req, resp);
                RequestContext.Response.OutputStream.Close();
            }
            catch (Exception ex)
            {
                // "Internal server error"
                RequestContext.Response.StatusCode = 500;
                RequestContext.Response.StatusDescription = ex.Message;
            }
        }
    }

    /// <summary>
    /// Provides access to .Net HttpListenerRequest classes
    /// </summary>
    public class ListenerRequest : CookComputing.XmlRpc.IHttpRequest
    {
        public ListenerRequest(HttpListenerRequest request)
        {
            this.request = request;
        }

        public Stream InputStream
        {
            get { return request.InputStream; }
        }

        public string HttpMethod
        {
            get { return request.HttpMethod; }
        }

        private HttpListenerRequest request;
    }

    /// <summary>
    /// Provides access to the .Net HttpListenerResponse classes
    /// </summary>
    public class ListenerResponse : CookComputing.XmlRpc.IHttpResponse
    {
        public ListenerResponse(HttpListenerResponse response)
        {
            this.response = response;
        }

        string IHttpResponse.ContentType
        {
            get { return response.ContentType; }
            set { response.ContentType = value; }
        }

        TextWriter IHttpResponse.Output
        {
            get { return new StreamWriter(response.OutputStream); }
        }

        Stream IHttpResponse.OutputStream
        {
            get { return response.OutputStream; }
        }

        int IHttpResponse.StatusCode
        {
            get { return response.StatusCode; }
            set { response.StatusCode = value; }
        }

        string IHttpResponse.StatusDescription
        {
            get { return response.StatusDescription; }
            set { response.StatusDescription = value; }
        }

        bool IHttpResponse.SendChunked
        {
            get { return true; }
            set { response.SendChunked = value; }
        }

        long IHttpResponse.ContentLength
        {
            set { response.ContentLength64 = value; }
        }

        private HttpListenerResponse response;
    }

    /*****************************************(end) XML-RPC Server stuff ********************************************************************************/

    /***************************************** XML-RPC Client stuff ********************************************************************************/

    /// <summary>
    /// This defines the various methods on the GIFT XML-RPC server that can be called in this C# application
    /// </summary>
    public interface ISumAndDiff : IXmlRpcProxy
    {

        /// <summary>
        /// This method is used when running the GIFT SimpleExamplePluginInterface interop plugin class in a GIFT course.
        /// It will allow this application to send content to GIFT via an XML-RPC call.
        /// </summary>
        /// <param name="content"></param>
        [XmlRpcMethod("mil.arl.gift.gateway.interop.simple.SimpleExampleTAPluginInterface$SimpleExampleTAPluginXMLRPC.handleTrainingApplicationMessage")]
        void trainingApplicationStateMessage(String content);

        /// <summary>
        /// This method is used when running the GIFT SimpleExamplePluginInterface interop plugin class in a GIFT course.
        /// It will allow this application to notify GIFT of a 'finished' condition via an XML-RPC call.
        /// </summary>
        /// <param name="content"></param>
        [XmlRpcMethod("mil.arl.gift.gateway.interop.simple.SimpleExampleTAPluginInterface$SimpleExampleTAPluginXMLRPC.handleTrainingApplicationFinished")]
        void trainingApplicationFinished();

        /// <summary>
        /// This method is used when running the GIFT mil.arl.gift.net.simple.XMLRPCServer main method which
        /// uses the inner Example class to handle RPC client requests from this application.
        /// </summary>
        /// <param name="content"></param>
        /// <returns></returns>
        [XmlRpcMethod("mil.arl.gift.net.xmlrpc.XMLRPCServer$Example.test")]
        String test(String content);
    }

    /*****************************************(end) XML-RPC Client stuff ********************************************************************************/
}
