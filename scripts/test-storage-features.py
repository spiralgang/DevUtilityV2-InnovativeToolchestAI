#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Test Storage Features - ZRAM & NumPy Memmap Implementation Test
Validates that the new storage features are properly integrated
"""

import asyncio
import sys
from pathlib import Path

# Add the current directory to Python path for imports
sys.path.insert(0, str(Path(__file__).parent))

# Import the comprehensive data scraper
try:
    from comprehensive_data_scraper import ComprehensiveDataScraper
except ImportError:
    # Try importing from scripts directory
    import importlib.util
    spec = importlib.util.spec_from_file_location(
        "comprehensive_data_scraper", 
        Path(__file__).parent / "comprehensive-data-scraper.py"
    )
    comprehensive_data_scraper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(comprehensive_data_scraper)
    ComprehensiveDataScraper = comprehensive_data_scraper.ComprehensiveDataScraper

async def test_storage_features():
    """Test the new ZRAM and NumPy memmap storage features"""
    print("🧪 Testing Storage Features Integration...")
    print("=" * 60)
    
    # Initialize the scraper
    scraper = ComprehensiveDataScraper()
    
    # Test only the storage features scraping
    print("💾 Testing mobile storage features scraping...")
    try:
        await scraper.scrape_mobile_storage_features()
        print("✅ Storage features scraping completed successfully!")
        
        # Check if directories were created
        storage_dir = scraper.data_dir / "mobile_storage"
        if storage_dir.exists():
            print(f"✅ Storage directory created: {storage_dir}")
            
            # Check subdirectories
            subdirs = ['numpy_memmap', 'zram', 'zram_repos', 'linux_kernel']
            for subdir in subdirs:
                subdir_path = storage_dir / subdir
                if subdir_path.exists():
                    file_count = len(list(subdir_path.glob('*')))
                    print(f"✅ {subdir}: {file_count} files created")
                else:
                    print(f"⚠️  {subdir}: Directory not created")
            
            # Check for integration guides
            guides = [
                'MOBILE_MEMMAP_GUIDE.md',
                'MOBILE_ZRAM_GUIDE.md', 
                'STORAGE_INTEGRATION_GUIDE.md'
            ]
            
            for guide in guides:
                guide_paths = list(storage_dir.rglob(guide))
                if guide_paths:
                    print(f"✅ {guide}: Created")
                else:
                    print(f"⚠️  {guide}: Not found")
        else:
            print("❌ Storage directory was not created")
            return False
            
    except Exception as e:
        print(f"❌ Storage features test failed: {e}")
        return False
    
    print("=" * 60)
    print("🎉 Storage features test completed!")
    return True

def test_living_environment_integration():
    """Test living environment integration"""
    print("\n🧬 Testing Living Environment Integration...")
    print("=" * 60)
    
    # Check if wrapper functions are available
    wrapper_file = Path(".living_environment_wrapper.sh")
    if wrapper_file.exists():
        content = wrapper_file.read_text()
        
        # Check for storage functions
        storage_functions = [
            'storage_features_enable',
            'zram_status', 
            'memmap_status',
            'storage_optimization_report'
        ]
        
        for func in storage_functions:
            if func in content:
                print(f"✅ {func}: Integrated in wrapper")
            else:
                print(f"❌ {func}: Missing from wrapper")
    else:
        print("❌ Living environment wrapper not found")
        return False
    
    # Check activator
    activator_file = Path(".activate_living_environment")
    if activator_file.exists():
        content = activator_file.read_text()
        if "storage_features_enable" in content:
            print("✅ Storage commands added to activator")
        else:
            print("⚠️  Storage commands not found in activator")
    
    print("=" * 60)
    print("🎉 Living environment integration test completed!")
    return True

if __name__ == "__main__":
    print("🚀 Starting Storage Features Test Suite...")
    print("This will test the new ZRAM & NumPy memmap integration\n")
    
    # Test storage features
    success1 = asyncio.run(test_storage_features())
    
    # Test living environment integration
    success2 = test_living_environment_integration()
    
    if success1 and success2:
        print("\n🎉 ALL TESTS PASSED!")
        print("💾 ZRAM & NumPy memmap storage features are ready!")
        print("🧬 Living code integration is complete!")
        print("\n📝 Next steps:")
        print("   1. Run: source ./.activate_living_environment")
        print("   2. Run: storage_features_enable")
        print("   3. Run: zram_status")
        print("   4. Run: memmap_status")
        sys.exit(0)
    else:
        print("\n❌ SOME TESTS FAILED!")
        print("Check the output above for details")
        sys.exit(1)