
# Quản lý chuỗi cửa hàng bán trà sữa UTEADRINK
## Mô tả dự án 
UTEADRINK là một nền tảng thương mại điện tử được thiết kế để quản lý chuỗi cửa hàng bán trà sữa, cung cấp trải nghiệm mua sắm trực tuyến mượt mà cho khách hàng và công cụ quản lý mạnh mẽ cho các chủ cửa hàng. Dự án này không chỉ giúp các cửa hàng quản lý sản phẩm, đơn hàng và khách hàng một cách hiệu quả, mà còn hỗ trợ người dùng trong việc thanh toán trực tuyến và giao tiếp với cửa hàng qua các tính năng chat thời gian thực.

## Hướng dẫn cài đặt dự án
### 1. Yêu cầu hệ thống
- Java 17 hoặc các phiên bản cao hơn
- Maven 3.x hoặc Gradle
- SQL Server 
### 2. Cài đặt bước đầu
#### Clone Repository
```bash
git clone https://github.com/tuyetminhminh/UTeaDrinkWebsite.git
```
#### Cài đặt các phụ thuộc với Maven
```bash
mvn install
```
#### Cấu hình kết nối cơ sở dữ liệu trong application.properties
```javascript
spring.datasource.url=jdbc:sqlserver://<Tên máy bạn>; databaseName=<NameDatabase>;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true;characterEncoding=UTF-8
spring.datasource.username= <Tên đăng nhập của bạn>
spring.datasource.password= <Mật khẩu truy cập>
```
#### Chạy ứng dụng
```bash
mvn spring-boot:run
```


## Cấu trúc dự án
```javascript
src/
├── main/
│   ├── java/
│   │   ├── net.codejava.utea/
│   │   │   ├── admin/
│   │   │   ├── ai/
│   │   │   ├── auth/
│   │   │   ├── catalog/
│   │   │   ├── chat/
│   │   │   ├── common/
│   │   │   ├── config/
│   │   │   ├── customer/
│   │   │   ├── engament/
│   │   │   ├── manager/
│   │   │   ├── media.service/
│   │   │   ├── notify/
│   │   │   ├── order/
│   │   │   ├── payment/
│   │   │   ├── promotion/
│   │   │   ├── review/
│   │   │   ├── shipper/
│   │   │   ├── shipping/
│   │   │   ├── viewview/
│   └── resources/
│       ├── application.properties
│       ├── static/
│       ├── templates
└── pom.xml
```
## Các tính năng chính
### Quản lý sản phẩm và danh mục
Nền tảng cung cấp công cụ quản lý sản phẩm dễ dàng, cho phép các cửa hàng thêm, sửa, xóa và phân loại sản phẩm. Từng sản phẩm được liên kết với các thông tin chi tiết như tên, giá cả, mô tả, hình ảnh và các thuộc tính khác. Người dùng có thể tìm kiếm và lọc sản phẩm theo các tiêu chí như danh mục, giá, độ phổ biến, v.v.

### Giỏ hàng và thanh toán trực tuyến
Hệ thống cho phép người dùng thêm sản phẩm vào giỏ hàng và tiến hành thanh toán qua các phương thức trực tuyến như VNPay. Quy trình thanh toán đơn giản và nhanh chóng, giúp khách hàng hoàn tất giao dịch mà không gặp phải rắc rối. Các chi tiết về đơn hàng, thuế, phí vận chuyển và các khuyến mãi được tính toán tự động.

### Quản lý đơn hàng
Các cửa hàng có thể theo dõi trạng thái của đơn hàng từ khi đặt hàng đến khi giao hàng, bao gồm việc quản lý lịch sử đơn hàng và trạng thái từng đơn. Hệ thống cho phép các cửa hàng kiểm soát và xác nhận đơn hàng, cũng như thông báo cho khách hàng khi đơn hàng thay đổi trạng thái.

### Tính năng chat thời gian thực
Dự án tích hợp WebSocket để hỗ trợ tính năng chat thời gian thực giữa khách hàng và cửa hàng. Điều này cho phép các cửa hàng giao tiếp trực tiếp với khách hàng, giải đáp thắc mắc và xử lý yêu cầu hỗ trợ ngay lập tức. Khách hàng có thể gửi câu hỏi về sản phẩm, trạng thái đơn hàng hoặc yêu cầu hỗ trợ kỹ thuật, và nhận được phản hồi ngay lập tức.

### Trợ lý ảo hỗ trợ khách hàng
UTEADRINK được tích hợp với một trợ lý ảo thông minh, giúp giải đáp các câu hỏi thường gặp và hỗ trợ khách hàng trong suốt quá trình mua sắm. Trợ lý ảo có thể giúp khách hàng tìm kiếm sản phẩm, hướng dẫn thanh toán, cung cấp thông tin về chính sách giao hàng và thậm chí xử lý các vấn đề sau bán hàng.

### Quản lý người dùng và quyền hạn
Hệ thống hỗ trợ nhiều loại tài khoản với quyền hạn khác nhau như người dùng, quản trị viên và nhà cung cấp. Các nhà quản lý có thể dễ dàng quản lý tài khoản người dùng, cấp quyền truy cập cho nhân viên và theo dõi hoạt động của các cửa hàng.

### Tính năng đánh giá và nhận xét sản phẩm
Hệ thống cho phép người dùng đánh giá sản phẩm và để lại nhận xét về trải nghiệm của họ. Điều này giúp các khách hàng khác đưa ra quyết định mua sắm chính xác hơn, đồng thời giúp cửa hàng cải thiện chất lượng dịch vụ và sản phẩm.

### Bảo mật và xác thực
Dự án sử dụng Spring Security kết hợp với JWT (JSON Web Token) để bảo vệ các tài khoản người dùng và đảm bảo an toàn trong quá trình xác thực. Người dùng có thể đăng ký tài khoản, đăng nhập và duy trì phiên làm việc một cách an toàn. Các mã OTP cũng được tích hợp để hỗ trợ quá trình xác thực 2 yếu tố (2FA) cho các tài khoản quan trọng.

### Quản lý khuyến mãi và giảm giá
Các cửa hàng có thể tạo và quản lý các chương trình khuyến mãi, mã giảm giá cho khách hàng, đồng thời theo dõi hiệu quả của các chiến dịch này qua các báo cáo thống kê.
## Các công nghệ sử dụng
### Spring Boot 
Dùng để xây dựng backend với các tính năng mạnh mẽ như bảo mật, xác thực, quản lý cơ sở dữ liệu và xử lý HTTP request.

### Thymeleaf 
Một thư viện Java để tạo giao diện người dùng động trên frontend.

### WebSocket
Được sử dụng để cung cấp tính năng chat thời gian thực, giúp cửa hàng và khách hàng giao tiếp ngay lập tức.

### JWT (JSON Web Token) 
Được sử dụng để xác thực người dùng và bảo vệ các API.

### Cloudinary 
Được sử dụng để quản lý và lưu trữ hình ảnh sản phẩm và ảnh đại diện của người dùng.
## Hướng dẫn đóng góp
Nếu bạn muốn mọi người tham gia vào dự án, hãy cung cấp các hướng dẫn cụ thể về cách đóng góp. Điều này giúp người dùng dễ dàng đóng góp vào mã nguồn hoặc cải tiến dự án.

### Các bước đóng góp:

#### Fork repo này
#### Tạo nhánh mới
```javascript
git checkout -b feature-branch
```
#### Thực hiện thay đổi và commit
```javascript
git commit -am 'Add new feature
```
#### Push nhánh của bạn
```javascript
git push origin feature-branch
```
#### Mở Pull Request và mô tả các thay đổi.


## Một số hình ảnh giao diện của hệ thốngthống
### 1. Giao diện trang chủ 
![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754077/Screenshot_2025-10-29_224358_iai98c.png)

### 2. Giao diện khách hàng 
![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754075/Screenshot_2025-10-29_225021_nlhcew.png)

![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754066/Screenshot_2025-10-29_225133_dcrsic.png)


### 3. Giao diện quản lí cửa hàng 
![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754063/Screenshot_2025-10-29_225411_kuxf76.png)


![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754080/Screenshot_2025-10-29_225909_n5aawj.png)



![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754066/Screenshot_2025-10-29_225608_x2n9wu.png)

![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754069/Screenshot_2025-10-29_225754_t4nkum.png)



![Logo UTeaDrink](https://res.cloudinary.com/dmgr8squx/image/upload/v1761754063/Screenshot_2025-10-29_225510_br7omb.png)

## Tổng kết
**UTEADRINK mang đến một giải pháp toàn diện giúp các cửa hàng trà sữa nâng cao trải nghiệm khách hàng và tối ưu hóa quy trình kinh doanh của mình. Dự án này không chỉ đơn thuần là một nền tảng bán hàng mà còn là một công cụ hỗ trợ giúp các cửa hàng quản lý, vận hành và phát triển bền vững trong môi trường cạnh tranh hiện nay**
## Link demo trang web
[Truy cập Trang web UteaDrink](https://uteadrink.onrender.com/)
