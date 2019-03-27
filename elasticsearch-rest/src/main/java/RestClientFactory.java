import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;

/**
 * Author: pgf
 * DateTime: 2019/3/25 19:44
 * Description:
 */
public class RestClientFactory {
    private static RestClient restClient;
    private RestClientFactory (){}
    public static RestClient getRestClient(){
        if(restClient == null){
            synchronized (RestClientFactory.class){
                if(restClient == null){
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("elastic","elastic"));
                    SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
                    restClient = RestClient.builder(
                                new HttpHost("localhost", 9200),
                                new HttpHost("localhost2", 9200)
                             )
                            .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                                @Override
                                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                                }
                            })
                            .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                                @Override
                                public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                                    return builder.setConnectTimeout(5000)//默认连接超时1秒
                                            .setSocketTimeout(60000);//默认超时30秒
                                }
                            })
                            .setMaxRetryTimeoutMillis(60000)//默认重试超时60秒
                            .setFailureListener(sniffOnFailureListener)
                            .build();
                    Sniffer sniffer = Sniffer.builder(restClient)
                            .setSniffIntervalMillis(60000).setSniffAfterFailureDelayMillis(30000).build();
                    sniffOnFailureListener.setSniffer(sniffer);
                }
            }

        }
        return restClient;
    }
}
