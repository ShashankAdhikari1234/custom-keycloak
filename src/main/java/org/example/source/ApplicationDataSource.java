package org.example.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class ApplicationDataSource {

    @Autowired
    private DataSource defaultDataSource;

//    @PostConstruct
//    public void initializeDataSource() throws SQLException {
//        try (Connection connection = defaultDataSource.getConnection();
//             PreparedStatement statement = connection.prepareStatement("SELECT * FROM tenant_information WHERE name = ?")) {
//            // Replace "default" with the desired tenant name if needed.
//            statement.setString(1, "default");
//
//            try (ResultSet rs = statement.executeQuery()) {
//                if (rs.next()) {
//                    TenantConfigurationEntity config = TenantConfigurationEntity.builder()
//                            .id(rs.getLong("id"))
//                            .url(rs.getString("url"))
//                            .username(rs.getString("username"))
//                            .driverClassName(rs.getString("driver_class_name"))
//                            .initialize(rs.getBoolean("initialize"))
//                            .password(rs.getString("password"))
//                            .name(rs.getString("name"))
//                            .build();
//
//                    HikariConfig hikariConfig = DataSourceConfigUtil.setDataSourceEnvConfig(config);
//                    this.dataSource = new HikariDataSource(hikariConfig);
//                }
//            }
//        }
//    }
//
//    public ApplicationDataSource getDataSource() {
//        return dataSource;
//    }

}
