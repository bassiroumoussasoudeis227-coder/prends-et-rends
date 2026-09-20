import SwiftUI
import MapKit

public struct SatelliteMapView: View {
    let latitude: Double
    let longitude: Double
    let address: String?
    let personName: String
    
    @State private var isHybrid: Bool = false
    
    public init(
        latitude: Double,
        longitude: Double,
        address: String?,
        personName: String
    ) {
        self.latitude = latitude
        self.longitude = longitude
        self.address = address
        self.personName = personName
    }
    
    private var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            // Map area
            ZStack(alignment: .topLeading) {
                Map(coordinateRegion: .constant(
                    MKCoordinateRegion(
                        center: coordinate,
                        span: MKCoordinateSpan(latitudeDelta: 0.005, longitudeDelta: 0.005)
                    )
                ), annotationItems: [MapPinItem(id: "pin", coordinate: coordinate)]) { item in
                    MapAnnotation(coordinate: item.coordinate) {
                        VStack(spacing: 2) {
                            Text(personName)
                                .font(.system(size: 10, weight: .bold))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.white.opacity(0.9))
                                .foregroundColor(.black)
                                .cornerRadius(4)
                            
                            Image(systemName: "mappin.circle.fill")
                                .font(.system(size: 26))
                                .foregroundColor(.red)
                                .background(Color.white.clipShape(Circle()))
                        }
                    }
                }
                .frame(height: 160)
                .cornerRadius(12)
                
                // Satellite indicator
                HStack(spacing: 4) {
                    Image(systemName: "globe.europe.africa.fill")
                        .font(.system(size: 11))
                        .foregroundColor(.blue)
                    Text("Satellite GPS")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.black.opacity(0.75))
                .cornerRadius(6)
                .padding(8)
            }
            
            // Bottom Action Bar with Address & Apple Maps Navigation
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(address ?? "\(String(format: "%.4f", latitude)), \(String(format: "%.4f", longitude))")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.white)
                        .lineLimit(1)
                    Text("Coordonnées GPS certifiées")
                        .font(.system(size: 10))
                        .foregroundColor(.gray)
                }
                
                Spacer()
                
                Button(action: openInAppleMaps) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.triangle.turn.up.right.diamond.fill")
                            .font(.system(size: 11))
                        Text("Plans")
                            .font(.system(size: 11, weight: .bold))
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color(red: 0.1, green: 0.12, blue: 0.18))
        }
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
        )
    }
    
    private func openInAppleMaps() {
        let placemark = MKPlacemark(coordinate: coordinate)
        let mapItem = MKMapItem(placemark: placemark)
        mapItem.name = "\(personName) - Prêt"
        mapItem.openInMaps(launchOptions: [
            MKLaunchOptionsMapTypeKey: MKMapType.hybrid.rawValue
        ])
    }
}

private struct MapPinItem: Identifiable {
    let id: String
    let coordinate: CLLocationCoordinate2D
}
